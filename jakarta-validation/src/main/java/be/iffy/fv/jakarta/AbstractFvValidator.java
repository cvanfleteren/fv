package be.iffy.fv.jakarta;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.collection.List;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.jspecify.annotations.NonNull;
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

    @NonNull // gets set on initialization
    protected Rule<Object> rule;

    // check to see if we have Hibernate Validator on the classpath without causing an error if we don't
    static final boolean HAS_HIBERNATE_VALIDATOR;
    static  {
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
        Object ctx = violationBuilder.addPropertyNode(paths.head().text());

        // For each pair (segment[i], segment[i+1]): add segment[i+1], then apply segment[i]'s index
        for (int i = 0; i < paths.size() - 1; i++) {
            ErrorMessage.Path prevSeg = paths.apply(i);
            String nextText = paths.apply(i + 1).text();

            // Add the next property node (addPropertyNode is available on both NBC and NBD)
            NodeBuilderCustomizableContext nextNBC = switch (ctx) {
                case NodeBuilderCustomizableContext nbc -> nbc.addPropertyNode(nextText);
                case NodeBuilderDefinedContext nbd -> nbd.addPropertyNode(nextText);
                default -> throw new IllegalStateException();
            };

            // Apply the previous segment's index to the newly added node (if any)
            ctx = prevSeg.index().fold(
                () -> (Object) nextNBC,
                idx -> applyIntermediateIndex(nextNBC, idx)
            );
        }

        // Handle the last segment's own index (if any): add an anonymous bean node at that index,
        // which HV renders as "fieldName[N]" when it is the terminal path node.
        ErrorMessage.Path last = paths.last();
        if (last.index().isDefined()) {
            applyTerminalIndex(ctx, last.index().get());
        } else {
            switch (ctx) {
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
            HibernateConstraintValidatorContext hvCtx = context.unwrap(HibernateConstraintValidatorContext.class);
            error.parameters().forEach(hvCtx::addMessageParameter);

            return hvCtx.buildConstraintViolationWithTemplate(template);
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
