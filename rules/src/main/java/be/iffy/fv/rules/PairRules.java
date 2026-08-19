package be.iffy.fv.rules;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import io.vavr.Tuple2;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Common validation rules for pairs of values, represented as {@link Tuple2}.
 */
public final class PairRules {

    /**
     * Singleton instance of {@link PairRules}.
     */
    public static final PairRules pairs = new PairRules();

    /**
     * Fails if the first element of the pair is not strictly before the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.before.second}
     */
    public <T extends Comparable<? super T>> Rule<Tuple2<T, T>> strictlyOrdered() {
        return ComparableRules.strictlyOrdered();
    }

    /**
     * Fails if the first element of the pair is not before or equal to the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.at.most.second}
     */
    public <T extends Comparable<? super T>> Rule<Tuple2<T, T>> ordered() {
        return ComparableRules.ordered();
    }

    /**
     * Fails if the first element of the pair is not strictly after the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.after.second}
     */
    public <T extends Comparable<? super T>> Rule<Tuple2<T, T>> strictlyDescending() {
        return ComparableRules.strictlyDescending();
    }

    /**
     * Fails if the first element of the pair is not after or equal to the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.at.least.second}
     */
    public <T extends Comparable<? super T>> Rule<Tuple2<T, T>> descending() {
        return ComparableRules.descending();
    }

    /**
     * Fails unless the two elements of the pair are equal.
     * <p>
     * Error key: {@code pair.must.be.equal}
     */
    public <T> Rule<Tuple2<T, T>> equal() {
        return Rule.of(
                t -> Objects.equals(t._1, t._2),
                ErrorMessage.of("pair.must.be.equal")
        );
    }

    /**
     * Fails if the two elements of the pair are equal.
     * <p>
     * Error key: {@code pair.must.not.be.equal}
     */
    public <T> Rule<Tuple2<T, T>> notEqual() {
        return Rule.of(
                t -> !Objects.equals(t._1, t._2),
                ErrorMessage.of("pair.must.not.be.equal")
        );
    }

    /**
     * Fails unless the two elements of the pair satisfy the specified predicate.
     * <p>
     * Error key: the {@code errorKey} passed as argument.
     *
     * @param predicate the predicate to apply to the two elements of the pair.
     * @param errorKey  the error key to use if the predicate returns {@code false}.
     */
    public <T1, T2> Rule<Tuple2<T1, T2>> satisfies(BiPredicate<? super T1, ? super T2> predicate, String errorKey) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.of(
                t -> predicate.test(t._1, t._2),
                errorKey
        );
    }

    /**
     * Fails unless the two elements of the pair satisfy the specified predicate.
     * <p>
     * Error key: the {@code errorMessage} passed as argument.
     *
     * @param predicate    the predicate to apply to the two elements of the pair.
     * @param errorMessage the error message to use if the predicate returns {@code false}.
     */
    public <T1, T2> Rule<Tuple2<T1, T2>> satisfies(BiPredicate<? super T1, ? super T2> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.of(
                t -> predicate.test(t._1, t._2),
                errorMessage
        );
    }

}
