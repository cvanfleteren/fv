package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import io.vavr.control.Try;

/**
 * BV {@link jakarta.validation.ConstraintValidator} that resolves an FV {@link Rule} from a
 * {@code public static} field on a class, as specified by {@link FvStaticRule}.
 */
public class FvStaticRuleValidator extends AbstractFvValidator<FvStaticRule> {

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(FvStaticRule annotation) {
        rule = (Rule<Object>) resolveRule(annotation.on(), annotation.field()).get();
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
