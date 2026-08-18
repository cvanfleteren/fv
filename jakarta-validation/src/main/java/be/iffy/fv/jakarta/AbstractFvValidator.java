package be.iffy.fv.jakarta;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Try;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import jakarta.validation.ValidationException;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Abstract base class for a Bean Validation (BV) {@link ConstraintValidator} implementation
 * that integrates with Functional Validation (FV) via {@link Rule}. This class provides
 * common logic for applying FV rules to validate objects and properly handle validation errors
 * in the context of BV.
 *
 * @param <A> The type of the annotation to use as metadata for the validator.
 */
abstract class AbstractFvValidator<A extends Annotation> implements ConstraintValidator<A, Object> {

    // volatile because FvStaticRuleValidator may initialize lazily in isValid() from a different
    // thread than the one that called initialize().
    protected volatile Rule<Object> rule;

    // check to see if we have Hibernate Validator on the classpath without causing an error if we don't
    static final boolean HAS_HIBERNATE_VALIDATOR;

    static {
        boolean found;
        try {
            Class.forName("org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext", false, AbstractFvValidator.class.getClassLoader());
            found = true;
        } catch (ClassNotFoundException e) {
            found = false;
        }
        HAS_HIBERNATE_VALIDATOR = found;
    }

    @Override
    public boolean isValid(@Nullable Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        Validation<?> result = Objects.requireNonNull(rule.apply(value), "Rules are not allowed to return a null Validation");

        return result.fold(
            errors -> {
                context.disableDefaultConstraintViolation();
                errors.forEach(error -> addViolation(error, context));
                return false;
            },
            ignore -> true
        );
    }

    protected static Rule<?> getRule(Class<?> cls, Object instance) {
        return switch (instance) {
            case Rule<?> r -> r;
            case RuleProvider<?> p -> {
                Rule<?> r = p.provide();
                if (r == null) {
                    throw new IllegalArgumentException(cls.getName() + ".provide() returned null");
                }
                yield r;
            }
            case null, default ->
                throw new IllegalArgumentException(cls.getName() + " must implement Rule or RuleProvider");
        };
    }

    private void addViolation(ErrorMessage error, ConstraintValidatorContext context) {
        ConstraintViolationBuilder violationBuilder = buildViolation(error, context);
        List<ErrorMessage.Path> paths = error.paths();

        if (paths.isEmpty()) {
            violationBuilder.addConstraintViolation();
            return;
        }

        // Build the BV property path from the FV error path.
        //
        // Key constraint: HV renders the index associated with a node X as belonging to X's PARENT.
        // Specifically, addPropertyNode("parent").addPropertyNode("child").inIterable().atIndex(N)
        // renders as "parent[N].child", NOT "parent.child[N]".
        //
        // Therefore, FV's index on segment[i] (meaning "segment[i] is a container, element N")
        // must be expressed via inIterable().atIndex(N) on segment[i+1] — not on segment[i].
        //
        // The accumulator alternates between NodeBuilderCustomizableContext (no pending index, can
        // call inIterable()) and NodeBuilderDefinedContext (index just applied, cannot call inIterable()
        // but can call addPropertyNode/addConstraintViolation). We use Object to hold either.

        // Add the first segment
        Object initialCtx = violationBuilder.addPropertyNode(paths.head().text());

        // For each consecutive pair (segment[i], segment[i+1]): add segment[i+1], then apply segment[i]'s index
        // zip pairs each element with its successor, foldLeft accumulates the mutable context through the path
        Object almostCtx = paths.zip(paths.tail()).foldLeft(initialCtx, (ctx, pair) -> {
            ErrorMessage.Path prev = pair._1;
            ErrorMessage.Path next = pair._2;

            // Add the next property node (addPropertyNode is available on both NBC and NBD)
            NodeBuilderCustomizableContext nextNBC = switch (ctx) {
                case NodeBuilderCustomizableContext nbc -> nbc.addPropertyNode(next.text());
                case NodeBuilderDefinedContext nbd -> nbd.addPropertyNode(next.text());
                default -> throw new IllegalStateException();
            };

            // Apply the previous segment's index to the newly added node (if any)
            return prev.index().fold(
                () -> (Object) nextNBC,
                idx -> applyIntermediateIndex(nextNBC, idx)
            );
        });

        // Handle the last segment's own index (if any): add an anonymous bean node at that index,
        // which HV renders as "fieldName[N]" when it is the terminal path node.
        ErrorMessage.Path last = paths.last();
        if (last.index().isDefined()) {
            applyTerminalIndex(almostCtx, last.index().get());
        } else {
            switch (almostCtx) {
                case NodeBuilderCustomizableContext nbc -> nbc.addConstraintViolation();
                case NodeBuilderDefinedContext nbd -> nbd.addConstraintViolation();
                default -> throw new IllegalStateException();
            }
        }
    }

    /**
     * Builds a violation for the given error, pushing FV parameters into Hibernate Validator's
     * message interpolation context when available. Falls back to bare key interpolation when
     * a different BV implementation is in use.
     *
     * <p>The message template is wrapped as {@code {error.key()}} (standard BV convention) so
     * that a {@code ValidationMessages.properties} entry like
     * {@code must.have.min.length=Must have at least {min} characters} resolves correctly.
     */
    private ConstraintViolationBuilder buildViolation(
        ErrorMessage error, ConstraintValidatorContext context
    ) {
        String template = "{" + error.key() + "}";
        if (HAS_HIBERNATE_VALIDATOR) {
            return Try.<ConstraintViolationBuilder>of(() -> {
                HibernateConstraintValidatorContext hvCtx = context.unwrap(HibernateConstraintValidatorContext.class);
                error.parameters().forEach(hvCtx::addMessageParameter);
                return hvCtx.buildConstraintViolationWithTemplate(template);
            }).recover(
                ValidationException.class,
                context.buildConstraintViolationWithTemplate(template)
            ).get();
        } else {
            return context.buildConstraintViolationWithTemplate(template);
        }
    }

    // Applies segment[i]'s index to segment[i+1]'s already-added property node. HV renders this
    // as "segment[i][idx].segment[i+1]" because the index visually shifts left by one node.
    private NodeBuilderDefinedContext applyIntermediateIndex(NodeBuilderCustomizableContext childNode, Object idx) {
        ConstraintViolationBuilder.NodeContextBuilder inIterable = childNode.inIterable();
        return switch (idx) {
            case Integer i -> inIterable.atIndex(i);
            default -> inIterable.atKey(idx.toString());
        };
    }

    // Adds an anonymous bean node at the given index, used for the terminal segment's own index.
    // Both NBC and NBD expose addBeanNode() → LeafNodeBuilderCustomizableContext, so we accept Object.
    private void applyTerminalIndex(Object ctx, Object idx) {
        ConstraintViolationBuilder.LeafNodeContextBuilder leafCtx = (switch (ctx) {
            case NodeBuilderCustomizableContext nbc -> nbc.addBeanNode();
            case NodeBuilderDefinedContext nbd -> nbd.addBeanNode();
            default -> throw new IllegalStateException();
        }).inIterable();
        switch (idx) {
            case Integer i -> leafCtx.atIndex(i).addConstraintViolation();
            default -> leafCtx.atKey(idx.toString()).addConstraintViolation();
        }
    }

}
