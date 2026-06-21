package be.iffy.fv.jakarta;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.List;

/**
 * BV {@link ConstraintValidator} that delegates to an FV {@link Rule}.
 *
 * <p>Instantiated by the BV runtime for each {@link FvRule}-annotated element.
 * The rule class is instantiated once per validator lifecycle via its public no-arg constructor.
 */
public class FvRuleValidator implements ConstraintValidator<FvRule, Object> {

    private Rule<Object> rule;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(FvRule annotation) {
        try {
            rule = (Rule<Object>) annotation.value().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                "Cannot instantiate Rule class " + annotation.value().getName()
                + " — ensure it has a public no-arg constructor.",
                e
            );
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
