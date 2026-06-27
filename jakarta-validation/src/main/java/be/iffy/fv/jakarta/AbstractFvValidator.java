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

        Validation<?> result = rule.apply(value);

        return result.fold(
            errors -> {
                context.disableDefaultConstraintViolation();
                result.errors().forEach(error -> addViolation(error, context));
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

        // Fold over adjacent pairs (current, next) to navigate intermediate segments.
        // Each step applies the current segment's index (if any) then advances to the next node.
        NodeBuilderCustomizableContext current = paths.init()
            .zip(paths.tail())
            .foldLeft(
                violationBuilder.addPropertyNode(paths.head().text()),
                (node, pair) -> pair._1.index().fold(
                    () -> node.addPropertyNode(pair._2.text()),
                    idx -> applyIntermediateIndex(node, idx).addPropertyNode(pair._2.text())
                )
            );

        // For the last segment: an index means "element N of this collection property".
        // We express this with an anonymous bean node (representing the element) at the index —
        // which HV renders as "fieldName[N]" in the path string.
        ErrorMessage.Path last = paths.last();
        if (last.index().isDefined()) {
            applyTerminalIndex(current, last.index().get());
        } else {
            current.addConstraintViolation();
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

    private NodeBuilderDefinedContext applyIntermediateIndex(NodeBuilderCustomizableContext ctx, Object idx) {
        ConstraintViolationBuilder.NodeContextBuilder nodeCtx = ctx.inIterable();
        return switch (idx) {
            case Integer i -> nodeCtx.atIndex(i);
            default -> nodeCtx.atKey(idx.toString());
        };
    }

    private void applyTerminalIndex(NodeBuilderCustomizableContext ctx, Object idx) {
        ConstraintViolationBuilder.LeafNodeContextBuilder leafCtx = ctx.addBeanNode().inIterable();
        switch (idx) {
            case Integer i -> leafCtx.atIndex(i).addConstraintViolation();
            default -> leafCtx.atKey(idx.toString()).addConstraintViolation();
        }
    }

}
