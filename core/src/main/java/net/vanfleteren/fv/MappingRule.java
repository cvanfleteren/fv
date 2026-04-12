package net.vanfleteren.fv;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a rule for mapping an input of type T to an output of type R,
 * with built-in validation support.
 * The mapping can either succeed (producing a {@link net.vanfleteren.fv.Validation.Valid} R) or fail (producing an {@link net.vanfleteren.fv.Validation.Invalid} with error details).
 *
 * @param <T> the type of input to be mapped
 * @param <R> the type of output after successful mapping
 */
@FunctionalInterface
public interface MappingRule<T, R> {

    /**
     * Evaluates the input against this rule, transforming it from type T to type R.
     *<pre>{@code
     * // 1. A rule that transforms a String into an Integer
     * // If parsing fails, it returns an Invalid validation with the specified error key
     * MappingRule<String, Integer> parseInt = MappingRule.ofTry(
     *     s -> Try.of(() -> Integer.parseInt(s)),
     *     "not.a.number"
     * );
     *
     * // 2. Successful transformation: String "123" -> Integer 123
     * Validation<Integer> success = parseInt.test("123");
     * // Returns Valid(123)
     *
     * // 3. Failed transformation: String "abc" -> Invalid
     * Validation<Integer> failure = parseInt.test("abc");
     * // Returns Invalid(ErrorMessage("not.a.number"))
     *  }</pre>
     *
     *
     * @param value the value to be processed by this {@link MappingRule}
     * @return a {@link Validation} instance representing the outcome: either a {@link net.vanfleteren.fv.Validation.Valid}
     *         with the successfully transformed value or the errors encountered during
     *         mapping or validation
     */
    Validation<R> test(T value);

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error message.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A mapper that might throw an exception (e.g., parsing an integer)
     * Function<String, Integer> parser = Integer::parseInt;
     *
     * // 2. Create a rule that catches exceptions and uses a specific error message
     * MappingRule<String, Integer> rule = MappingRule.of(parser, "invalid.number");
     *
     * // 3. Usage
     * rule.test("123");  // Returns Valid(123)
     * rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
     * }</pre>
     *
     * @param <T>      the type of input to be mapped
     * @param <R>      the type of output after mapping
     * @param mapper   the function that maps T to R
     * @param errorKey the errorKey to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> of(Function<T, R> mapper, String errorKey) {
        return of(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper returns a {@link Try.Failure}, the rule will fail with the specified error key.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A mapper that returns a Try (e.g., parsing an integer which might throw)
     * Function<String, Try<Integer>> parser = s -> Try.of(() -> Integer.parseInt(s));
     *
     * // 2. Create a rule that handles the Try and uses a specific error key
     * MappingRule<String, Integer> rule = MappingRule.ofTry(parser, ErrorMessage.of("invalid.number"));
     *
     * // 3. Usage
     * rule.test("123");  // Returns Valid(123)
     * rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
     * }</pre>
     *
     * @param <T>          the type of input to be mapped
     * @param <R>          the type of output after mapping
     * @param mapper       the function that maps T to R, returning a Try
     * @param errorMessage the errorMessage to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> ofTry(Function<T, Try<R>> mapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            Try<R> _try = mapper.apply(input);
            return _try.fold(
                    t -> Validation.invalid(errorMessage),
                    Validation::valid
            );
        };
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper returns a {@link Try.Failure}, the rule will fail with the specified error key.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A mapper that returns a Try (e.g., parsing an integer which might throw)
     * Function<String, Try<Integer>> parser = s -> Try.of(() -> Integer.parseInt(s));
     *
     * // 2. Create a rule that handles the Try and uses a specific error key
     * MappingRule<String, Integer> rule = MappingRule.ofTry(parser, "invalid.number");
     *
     * // 3. Usage
     * rule.test("123");  // Returns Valid(123)
     * rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
     * }</pre>
     *
     * @param <T>      the type of input to be mapped
     * @param <R>      the type of output after mapping
     * @param mapper   the function that maps T to R
     * @param errorKey the errorKey to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> ofTry(Function<T, Try<R>> mapper, String errorKey) {
        return ofTry(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error message.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A mapper that might throw an exception (e.g., parsing an integer)
     * Function<String, Integer> parser = Integer::parseInt;
     *
     * // 2. Create a rule that catches exceptions and uses a specific error message
     * ErrorMessage error = ErrorMessage.of("invalid.number");
     * MappingRule<String, Integer> rule = MappingRule.of(parser, error);
     *
     * // 3. Usage
     * rule.test("123");  // Returns Valid(123)
     * rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
     * }</pre>
     *
     * @param <T>            the type of input to be mapped
     * @param <R>            the type of output after mapping
     * @param throwingMapper the function that maps T to R
     * @param errorMessage   the error message to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> of(Function<T, R> throwingMapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(throwingMapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            Option<R> result = Function1.lift(throwingMapper).apply(input);
            return result.fold(
                    () -> Validation.invalid(errorMessage),
                    value -> Validation.valid(value)
            );
        };
    }

    /**
     * Returns a composed MappingRule that represents a shortcut-if-this rule.
     * This rule first applies the current rule to the input, and if successful,
     * applies the next rule (the argument to this method) to the result of the first rule.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that parses a String to an Integer
     * MappingRule<String, Integer> parseInt = s -> {
     *     try {
     *         return Validation.valid(Integer.parseInt(s));
     *     } catch (NumberFormatException e) {
     *         return Validation.invalid("not.a.number");
     *     }
     * };
     *
     * // 2. A rule that validates if an Integer is positive
     * MappingRule<Integer, Integer> isPositive = i ->
     *     i > 0 ? Validation.valid(i) : Validation.invalid("not.positive");
     *
     * // 3. Chain them: Parse the string, then check if the resulting number is positive
     * MappingRule<String, Integer> parseAndCheckPositive = parseInt.andThen(isPositive);
     *
     * // 4. Usage
     * Validation<Integer> valid = parseAndCheckPositive.test("10");  // Returns Valid(10)
     * Validation<Integer> notPositive = parseAndCheckPositive.test("-5");  // Returns Invalid("not.positive")
     * Validation<Integer> notANumber = parseAndCheckPositive.test("abc"); // Returns Invalid("not.a.number")
     * }</pre>
     *
     * @param <Z>  the type of output from the next rule after transformation.
     * @param rule the rule to apply after this rule if this rule is successful.
     * @return a composed MappingRule that represents a shortcut-if-this rule.
     */
    default <Z> MappingRule<T, Z> andThen(MappingRule<? super R, ? extends Z> rule) {
        return (T input) -> this.test(input).flatMap(rule::test);
    }

    /**
     * Applies a mapping function to the result of this {@link MappingRule}.
     *
     * @param <Z> the type of the result after applying the mapping function.
     * @param mapper the function to apply to the result of this rule if the input passes the test.
     * @return a new {@link MappingRule} that applies the mapping function to the result.
     */
    default <Z> MappingRule<T, Z> map(Function<R, Z> mapper){
        return (T input) -> this.test(input).map(mapper);
    }

    /**
     * Maps the result of this rule to a constant value, ignoring the underlying value.
     *
     * @param <Z> the type of the mapped value.
     * @param value the constant value to map to.
     * @return a new MappingRule that maps the result of this rule to the specified constant value.
     */
    default <Z> MappingRule<T, Z> mapTo(Z value){
        return this.map(ignored -> value);
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
     * @return a new {@link MappingRule} instance.
     * @throws NullPointerException if {@code other} is null.
     */
    @SuppressWarnings("unchecked")
    default <S> MappingRule<T, S> orElse(MappingRule<? super T, ? extends S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<S> first = (Validation<S>) this.test(input);
            if (first.isValid()) {
                return first;
            }

            Validation<S> second = (Validation<S>) other.test(input);
            if (second.isValid()) {
                return second;
            }

            return Validation.invalid(first.errors().appendAll(second.errors()));
        };
    }

    /**
     * Returns a new {@link MappingRule} that first applies this rule, and if the input is invalid, falls back to the other rule.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that maps the string "A" to 1
     * MappingRule<String, Integer> ruleA = s ->
     *     "A".equals(s) ? Validation.valid(1) : Validation.invalid("not.A");
     *
     * // 2. A rule that maps the string "B" to 2
     * MappingRule<String, Integer> ruleB = s ->
     *     "B".equals(s) ? Validation.valid(2) : Validation.invalid("not.B");
     *
     * // 3. Use recoverWith to try ruleA, and fall back to ruleB if ruleA fails
     * MappingRule<String, Integer> combined = ruleA.recoverWith(ruleB);
     *
     * // 4. Usage
     * Validation<Integer> validA = combined.test("A"); // Returns Valid(1)
     * Validation<Integer> validB = combined.test("B"); // Returns Valid(2)
     * Validation<Integer> invalid = combined.test("C"); // Returns Invalid("not.B")
     * }</pre>
     *
     * @param <S> the type of valid output produced by the other rule
     * @param other the other rule to use as a fallback if this rule fails
     * @return a new MappingRule that first applies this rule, and if the input is invalid, falls back to the other rule
     */
    default <S> MappingRule<T, S> recoverWith(MappingRule<? super T, S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<R> first = this.test(input);
            if (first.isValid()) {
                return (Validation<S>) first;
            }

            return Validation.narrow(other.test(input));
        };
    }

    /**
     * Turns this rule (back) into a {@link Predicate}.
     *
     * @param <S> the target type.
     * @return a {@link Predicate} instance.
     */
    default <S extends T> Predicate<S> toPredicate() {
        return value -> test(value).isValid();
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link List} of T instead of a single T.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Define a mapping rule
     * MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");
     *
     * // 2. Lift it to apply to a list
     * MappingRule<List<String>, List<Integer>> listRule = toInt.liftToList();
     *
     * // 3. Usage
     * listRule.test(List.of("1", "2")); // Returns Valid(List(1, 2))
     * listRule.test(List.of("1", "a")); // Returns Invalid(ErrorMessage("not.a.number").atIndex(1))
     * }</pre>
     *
     * @return a new {@link MappingRule} instance.
     */
    default MappingRule<List<T>, List<R>> liftToList() {
        return values -> {
            List<Validation<R>> validations = values.map(this::test);
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validation.sequence(validations);
        };
    }

    /**
     * Lifts this {@link MappingRule} so it applies to an {@link Option} of T.
     * <p>
     * Semantics:
     * - None =&gt; {@code valid(None)} (nothing to validate)
     * - Some(x) =&gt; validate x, and return {@code valid(Some(x))} or {@code invalid(errors)}
     * <p>
     * Usage example:
     * <pre>{@code
     * MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");
     *
     * MappingRule<Option<String>, Option<Integer>> optionRule = toInt.liftToOption();
     *
     * optionRule.test(Option.some("1")); // Returns Valid(Some(1))
     * optionRule.test(Option.none());     // Returns Valid(None)
     * optionRule.test(Option.some("a")); // Returns Invalid("not.a.number")
     * }</pre>
     *
     * @return a new {@link MappingRule} instance.
     */
    default MappingRule<Option<T>, Option<R>> liftToOption() {
        return opt -> opt
                .map(v -> this.test(v).map(Option::of))
                .getOrElse(() -> Validation.valid(Option.none()));
    }

    /**
     * Lifts this {@link MappingRule} so it applies to an {@link java.util.Optional} of T.
     * <p>
     * Semantics:
     * - empty =&gt; {@code valid(Optional.empty)} (nothing to validate)
     * - not empty =&gt; validate x, and return {@code valid(Optional.of(x))} or {@code invalid(errors)}
     * <p>
     * Usage example:
     * <pre>{@code
     * MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");
     *
     * MappingRule<Optional<String>, Optional<Integer>> optionalRule = toInt.liftToOptional();
     *
     * optionalRule.test(Optional.of("1")); // Returns Valid(Optional(1))
     * optionalRule.test(Optional.empty());   // Returns Valid(Optional.empty)
     * optionalRule.test(Optional.of("a")); // Returns Invalid("not.a.number")
     * }</pre>
     *
     * @return a new {@link MappingRule} instance.
     */
    default MappingRule<Optional<T>, Optional<R>> liftToOptional() {
        return opt -> opt
                .map(v -> this.test(v).map(Optional::of))
                .orElse(Validation.valid(Optional.empty()));
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link Map} of K to T.
     * <p>
     * Be careful, the key {@code key.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #liftToMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * <pre>{@code
     * MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");
     *
     * MappingRule<Map<String, String>, Map<String, Integer>> mapRule = toInt.liftToMap();
     *
     * mapRule.test(Map.of("k1", "1")); // Returns Valid(Map("k1", 1))
     * mapRule.test(Map.of("k1", "a")); // Returns Invalid(ErrorMessage("not.a.number").atIndex("k1"))
     * }</pre>
     *
     * @param <K> the key type.
     * @return a new {@link MappingRule} instance.
     */
    default <K> MappingRule<Map<K, T>, Map<K, R>> liftToMap() {
        return liftToMap(Objects::toString);
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * <pre>{@code
     * MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");
     *
     * MappingRule<Map<Integer, String>, Map<Integer, Integer>> mapRule = toInt.liftToMap(k -> "item-" + k);
     *
     * mapRule.test(Map.of(1, "a")); // Returns Invalid(ErrorMessage("not.a.number").atIndex("item-1"))
     * }</pre>
     *
     * @param keyExtractor the function to extract a path segment from the key.
     * @param <K>          the key type.
     * @return a new {@link MappingRule} instance.
     */
    default <K> MappingRule<Map<K, T>, Map<K, R>> liftToMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return map -> {
            Seq<Tuple2<K, Validation<R>>> validations = map.map(tuple ->
                    Tuple.of(tuple._1, this.test(tuple._2).mapErrors(errors ->
                            errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                    ))
            );

            var validAndInvalid = validations.partition(t -> t._2.isValid());
            if (validAndInvalid._2.nonEmpty()) {
                return Validation.invalid(validAndInvalid._2.flatMap(t -> t._2.errors()).toList());
            } else {
                return Validation.valid(validAndInvalid._1.toMap(Tuple2::_1, t -> t._2.getOrElseThrow()));
            }
        };
    }

    /**
     * Returns a {@link MappingRule} that checks if the input {@link Option} is defined and then applies the given rule to its value.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that checks if a string is not empty
     * MappingRule<String, String> notEmpty = s ->
     *     s.isEmpty() ? Validation.invalid("not.empty") : Validation.valid(s);
     *
     * // 2. A rule that requires the Option to be present before applying the rule
     * MappingRule<Option<String>, String> requiredString = MappingRule.requiredOption(notEmpty);
     *
     * // 3. Usage
     * Validation<String> valid = requiredString.test(Option.of("hello")); // Returns Valid("hello")
     * Validation<String> invalid = requiredString.test(Option.none());      // Returns Invalid("must.not.be.empty")
     * }</pre>
     *
     * @param <T>  the type of the value inside the {@link Option}
     * @param <R>  the type of the result of the mapping rule
     * @param rule the mapping rule to apply to the value inside the {@link Option}
     * @return a new {@link MappingRule} that validates the option and applies the given rule to its value
     */
    static <T, R> MappingRule<Option<T>, R> requiredOption(MappingRule<T, R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return rule.liftToOption().andThen(opt -> opt.fold(() -> Validation.invalid("must.not.be.empty"), Validation::valid));
    }

    /**
     * Returns a {@link MappingRule} that checks if the input {@link Optional} is defined and then applies the given rule to its value.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that checks if a string is not empty
     * MappingRule<String, String> notEmpty = s ->
     *     s.isEmpty() ? Validation.invalid("not.empty") : Validation.valid(s);
     *
     * // 2. A rule that requires the Option to be present before applying the rule
     * MappingRule<Optional<String>, String> requiredString = MappingRule.requiredOptional(notEmpty);
     *
     * // 3. Usage
     * Validation<String> valid = requiredString.test(Optional.of("hello")); // Returns Valid("hello")
     * Validation<String> invalid = requiredString.test(Optional.empty());      // Returns Invalid("must.not.be.empty")
     * }</pre>
     *
     * @param <T>  the type of the value inside the {@link Optional}
     * @param <R>  the type of the result of the mapping rule
     * @param rule the mapping rule to apply to the value inside the {@link Optional}
     * @return a new {@link MappingRule} that validates the optional and applies the given rule to its value
     */
    static <T, R> MappingRule<Optional<T>, R> requiredOptional(MappingRule<T, R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return rule.liftToOptional().andThen(opt -> opt.map(Validation::valid).orElseGet(() -> Validation.invalid("must.not.be.empty")));
    }

    /**
     * Returns a MappingRule that validates the input is not null.
     * <p>
     * Error key: "must.not.be.null"
     *
     * @param <T> the type of input and output
     * @return a MappingRule that returns valid input only if it's not null
     */
    static <T> MappingRule<T, T> notNull() {
        return input -> input == null ? Validation.invalid("must.not.be.null") : Validation.valid(input);
    }

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that validates a String and returns its length
     * MappingRule<String, Integer> lengthRule = s ->
     *     s.isEmpty() ? Validation.invalid("not.empty") : Validation.valid(s.length());
     *
     * // 2. A rule that applies 'lengthRule' to the 'name' property of a User
     * record User(String name) {}
     * MappingRule<User, Integer> userLengthRule = MappingRule.with(User::name, lengthRule);
     *
     * // 3. Usage
     * userLengthRule.test(new User("Alice")); // Returns Valid(5)
     * userLengthRule.test(new User(""));      // Returns Invalid("not.empty")
     * }</pre>
     *
     * @param <T>      the type of the input to be tested
     * @param <V>      the type of the result produced by the selector function
     * @param <R>      the type of the output produced by the rule
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    static <T, V, R> MappingRule<T, R> with(Function<T, V> selector, MappingRule<? super V, ? extends R> rule) {
        return input -> Validation.narrow(rule.test(selector.apply(input)));
    }
}
