package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import io.vavr.control.Try;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Modifier;

/**
 * BV {@link jakarta.validation.ConstraintValidator} that resolves an FV {@link Rule} from a
 * {@code public static} field on a class, as specified by {@link FvStaticRule}.
 *
 * <p>When {@link FvStaticRule#on()} is omitted (defaults to {@code Void.class}), the holder class
 * is inferred from the runtime type of the first validated value and cached for subsequent calls.
 * The write to the cached rule is safe under concurrent access because the computed value is always
 * the same for a given annotated element, and {@code rule} is {@code volatile} in the parent.
 */
public class FvStaticRuleValidator extends AbstractFvValidator<FvStaticRule> {

    // Non-null only when on == Void.class; cleared implicitly once rule is resolved.
    @Nullable
    private volatile String deferredField;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(FvStaticRule annotation) {
        if (annotation.on() != Void.class) {
            rule = (Rule<Object>) resolveRule(annotation.on(), annotation.field()).get();
        } else {
            deferredField = annotation.field();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(@Nullable Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (deferredField != null && rule == null) {
            rule = (Rule<Object>) resolveRule(value.getClass(), deferredField).get();
        }
        return super.isValid(value, context);
    }

    @SuppressWarnings("unchecked")
    static Try<Rule<?>> resolveRule(Class<?> holder, String fieldName) {
        if (fieldName.isEmpty()) {
            return Try.failure(new IllegalArgumentException(
                "@FvStaticRule requires a non-empty field name"
            ));
        }
        return Try.of(() -> holder.getDeclaredField(fieldName))
            .recoverWith(
                NoSuchFieldException.class,
                e -> Try.failure(new IllegalArgumentException("No field '" + fieldName + "' found on " + holder.getName(), e))
            )
            .andThenTry(f -> {
                if (!Modifier.isStatic(f.getModifiers())) {
                    throw new IllegalArgumentException(
                        "Field '" + fieldName + "' on " + holder.getName() + " must be static");
                }
            })
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
}
