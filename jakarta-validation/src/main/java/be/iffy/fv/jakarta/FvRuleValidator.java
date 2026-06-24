package be.iffy.fv.jakarta;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.control.Try;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

/**
 * BV {@link ConstraintValidator} that delegates to an FV {@link Rule}.
 *
 * <p>Instantiated by the BV runtime for each {@link FvRule}-annotated element.
 * Supports three rule-resolution modes; see {@link FvRule} for details.
 */
public class FvRuleValidator implements ConstraintValidator<FvRule, Object> {

    @Nullable
    private Rule<Object> rule;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(FvRule annotation) {
        rule = (Rule<Object>) resolveRule(annotation);
    }

    @Override
    public boolean isValid(@Nullable Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        Validation<?> result = rule.apply(value);
        if (result.isValid()) return true;

        context.disableDefaultConstraintViolation();
        result.errors().forEach(error -> addViolation(error, context));
        return false;
    }


    /**
     * Resolves the {@link Rule} described by the annotation, applying the same validation that
     * {@link #initialize} uses. Package-private so {@link FvRuleStartupValidator} can call it
     * during startup scanning without instantiating a full validator.
     *
     * @throws IllegalArgumentException if the annotation is misconfigured or the rule cannot be resolved
     */
    @SuppressWarnings("unchecked")
    static Rule<?> resolveRule(FvRule annotation) {
        boolean hasValue    = annotation.value()    != NoneRule.class;
        boolean hasProvider = annotation.provider() != NoneRuleProvider.class;
        boolean hasField    = annotation.on() != Void.class || !annotation.field().isEmpty();

        int modeCount = (hasValue ? 1 : 0) + (hasProvider ? 1 : 0) + (hasField ? 1 : 0);
        if (modeCount != 1) {
            throw new IllegalArgumentException(
                "@FvRule requires exactly one of: value, provider, or on+field — got " + modeCount
            );
        }

        if (hasValue) {
            return (Rule<?>) instantiate(annotation.value(), "Rule");
        } else if (hasProvider) {
            RuleProvider<?> p = (RuleProvider<?>) instantiate(annotation.provider(), "RuleProvider");
            Rule<?> r = p.provide();
            if (r == null) {
                throw new IllegalArgumentException(
                    annotation.provider().getName() + ".provide() returned null"
                );
            }
            return r;
        } else {
            return resolveStaticField(annotation.on(), annotation.field()).get();
        }
    }


    private static Object instantiate(Class<?> cls, String kind) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                "Cannot instantiate " + kind + " class " + cls.getName()
                + " — ensure it has a public no-arg constructor.",
                e
            );
        }
    }

    private static Try<Rule<?>> resolveStaticField(Class<?> holder, String fieldName) {
        if (holder == Void.class || fieldName.isEmpty()) {
            throw new IllegalArgumentException(
                "@FvRule static-field mode requires both 'on' and 'field' to be set"
            );
        }
        return Try.of(() -> holder.getDeclaredField(fieldName))
            .recoverWith(
                NoSuchFieldException.class,
                e -> Try.failure(new IllegalArgumentException("No field '" + fieldName + "' found on " + holder.getName(), e))
            )
            .andThenTry(f -> f.setAccessible(true))
            .mapTry(f -> f.get(null))
            .recoverWith(
                IllegalAccessException.class,
                e -> Try.failure(new IllegalArgumentException("Cannot read field '" + fieldName + "' on " + holder.getName(), e))
            )
            .flatMap(value -> {
                if (value == null) {
                    return Try.failure(new IllegalArgumentException(holder.getName() + "." + fieldName + " is null"));
                }
                if (!(value instanceof Rule<?> r)) {
                    return Try.failure(new IllegalArgumentException(
                        holder.getName() + "." + fieldName + " is not a Rule (found: " + value.getClass().getName() + ")"));
                }
                return Try.success(r);
            });
    }

    private void addViolation(ErrorMessage error, ConstraintValidatorContext context) {
        var violationBuilder = buildViolation(error, context);
        var paths = error.paths();

        if (paths.isEmpty()) {
            violationBuilder.addConstraintViolation();
            return;
        }

        // Fold over adjacent pairs (current, next) to navigate intermediate segments.
        // Each step applies the current segment's index (if any) then advances to the next node.
        var current = paths.init()
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
        var last = paths.last();
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
    private ConstraintValidatorContext.ConstraintViolationBuilder buildViolation(
        ErrorMessage error, ConstraintValidatorContext context
    ) {
        String template = "{" + error.key() + "}";
        try {
            HibernateConstraintValidatorContext hvCtx =
                context.unwrap(HibernateConstraintValidatorContext.class);
            error.parameters().forEach(hvCtx::addMessageParameter);
            return hvCtx.buildConstraintViolationWithTemplate(template);
        } catch (Exception ignored) {
            return context.buildConstraintViolationWithTemplate(template);
        }
    }

    private NodeBuilderDefinedContext applyIntermediateIndex(NodeBuilderCustomizableContext ctx, Object idx) {
        var nodeCtx = ctx.inIterable();
        return (idx instanceof Integer i) ? nodeCtx.atIndex(i) : nodeCtx.atKey(idx.toString());
    }

    private void applyTerminalIndex(NodeBuilderCustomizableContext ctx, Object idx) {
        var leafCtx = ctx.addBeanNode().inIterable();
        if (idx instanceof Integer i) {
            leafCtx.atIndex(i).addConstraintViolation();
        } else {
            leafCtx.atKey(idx.toString()).addConstraintViolation();
        }
    }
}
