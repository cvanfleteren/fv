### Project Guidelines - FV

This file contains standards and patterns established during the development of the FV project.

#### 1. Testing Standards
- **Framework**: Use JUnit 6 for testing.
- **Structure**: Use `@Nested` classes to group tests by record type or component.
- **Assertions**: Use AssertJ `assertThat()` syntax for better readability and failure messages.
- **Naming**: Use descriptive test names  that follow the method_context_expectation pattern, eg thisMethod_whenGivenInvalidInput_throwsException

#### 2. Code Style
- Follow existing patterns for `sealed interface` and `record` hierarchies.
- Maintain consistent package structures (e.g., `net.vanfleteren.fv.core`).
- Prefer functional programming style with immutability and side-effect free functions.
- Prefer Vavr types over standard Java types.
