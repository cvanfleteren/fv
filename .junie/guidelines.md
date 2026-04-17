### Project Guidelines - FV

This file contains standards and patterns established during the development of the FV project.

#### 1. Testing Standards
- **Framework**: Use JUnit 6 for testing.
- **Structure**: Use `@Nested` classes to group tests by record type or component.
- **Assertions**: Use AssertJ `assertThat()` syntax for better readability and failure messages.
- **Naming**: Use descriptive test names  that follow the method_context_expectation pattern, eg thisMethod_whenGivenInvalidInput_throwsException
- **Rules**: when testing Rule instances, always use errormessages that are seperated by dots, eg "invalid.input" or "value.must.not.be.null"

#### 2. Code Style
- Follow existing patterns for `sealed interface` and `record` hierarchies.
- Maintain consistent package structures (e.g., `net.vanfleteren.fv.core`).
- Prefer a functional programming style with immutability and side effect free functions.
- Prefer Vavr types over standard Java types (eg io.vavr.collection.* over java.util.*).
- nulls are generally considered to be invalid values.

 
#### 3. Design constraints
- Nulls are generally considered to be invalid values.
- Use instance methods for actual Rule methods in *Rules classes

### 4. Javadoc
- for Rules and MappingRules: always document the error key and parameters that will be used, as seen in this example:
```java
    /**
     * Fails if the value is less than the specified minimum.
     * <p>
     * Error key: {@code must.be.at.least}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed value ({@link BigDecimal})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     * @return a {@link Rule} checking the minimum value.
     */
    public Rule<BigDecimal> min(BigDecimal minInclusive) {
        return Rule.notNull().and(Rule.of(
                b -> b.compareTo(minInclusive) >= 0,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        ));
    }
```
