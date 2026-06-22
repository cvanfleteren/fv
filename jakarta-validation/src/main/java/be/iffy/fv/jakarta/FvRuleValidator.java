package be.iffy.fv.jakarta;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.List;

/**
 * BV {@link ConstraintValidator} that delegates to an FV {@link Rule}.
 *
 * <p>Instantiated by the BV runtime for each {@link FvRule}-annotated element.
 * Supports three rule-resolution modes; see {@link FvRule} for details.
 */
public class FvRuleValidator implements ConstraintValidator<FvRule, Object> {

    private Rule<Object> rule;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(FvRule annotation) {
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
            rule = (Rule<Object>) instantiate(annotation.value(), "Rule");
        } else if (hasProvider) {
            RuleProvider<Object> p = (RuleProvider<Object>) instantiate(annotation.provider(), "RuleProvider");
            rule = (Rule<Object>) p.provide();
            if (rule == null) {
                throw new IllegalArgumentException(
                    annotation.provider().getName() + ".provide() returned null"
                );
            }
        } else {
            rule = (Rule<Object>) resolveStaticField(annotation.on(), annotation.field());
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        Validation<?> result = rule.apply(value);
        if (result.isValid()) return true;

        context.disableDefaultConstraintViolation();
        result.errors().forEach(error -> addViolation(error, context));
        return false;
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

    private static Rule<?> resolveStaticField(Class<?> holder, String fieldName) {
        if (holder == Void.class || fieldName.isEmpty()) {
            throw new IllegalArgumentException(
                "@FvRule static-field mode requires both on and field to be set"
            );
        }
        Field f;
        try {
            f = holder.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(
                "No field '" + fieldName + "' found on " + holder.getName(), e
            );
        }
        f.setAccessible(true);
        Object value;
        try {
            value = f.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                "Cannot read field '" + fieldName + "' on " + holder.getName(), e
            );
        }
        if (value == null) {
            throw new IllegalArgumentException(
                holder.getName() + "." + fieldName + " is null"
            );
        }
        if (!(value instanceof Rule)) {
            throw new IllegalArgumentException(
                holder.getName() + "." + fieldName + " is not a Rule (found: " + value.getClass().getName() + ")"
            );
        }
        return (Rule<?>) value;
    }

    private void addViolation(ErrorMessage error, ConstraintValidatorContext context) {
        var violationBuilder = buildViolation(error, context);
        List<ErrorMessage.Path> paths = error.paths().toJavaList();

        if (paths.isEmpty()) {
            violationBuilder.addConstraintViolation();
            return;
        }

        var current = violationBuilder.addPropertyNode(paths.get(0).text());

        // For all but the last segment: apply index (if any) then add the next property node.
        // Index here marks the property as "in iterable at position N" which applies to
        // intermediate segments where we continue navigating deeper.
        for (int i = 0; i < paths.size() - 1; i++) {
            var nextName = paths.get(i + 1).text();
            if (paths.get(i).index().isDefined()) {
                current = applyIntermediateIndex(current, paths.get(i).index().get())
                    .addPropertyNode(nextName);
            } else {
                current = current.addPropertyNode(nextName);
            }
        }

        // For the last segment: an index means "element N of this collection property".
        // We express this with an anonymous bean node (representing the element) at the index —
        // which HV renders as "fieldName[N]" in the path string.
        var last = paths.get(paths.size() - 1);
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
