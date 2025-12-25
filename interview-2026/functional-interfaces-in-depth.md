# Functional Interfaces in Depth (Java)

## Definition
- A functional interface has exactly one abstract method (SAM). It may have default, static, or private methods without breaking the rule.
- Use `@FunctionalInterface` to get compile-time enforcement.
- Powers lambdas and method references.

## Built-in Families (java.util.function)
- **Functions:** `Function<T,R>`, `BiFunction<T,U,R>`, `UnaryOperator<T>` (Function<T,T>), `BinaryOperator<T>` (BiFunction<T,T,T>).
- **Predicates:** `Predicate<T>`, `BiPredicate<T,U>` (boolean tests).
- **Consumers:** `Consumer<T>`, `BiConsumer<T,U>` (side effects, void return).
- **Suppliers:** `Supplier<T>` (no args, produces T).
- **Primitive specializations:** `IntPredicate`, `IntFunction`, `ToIntFunction<T>`, `IntUnaryOperator`, `IntBinaryOperator`, plus Long/Double variants to avoid boxing.
- **Others:** `BooleanSupplier`, `ObjIntConsumer<T>`, `DoubleToIntFunction`, etc.

## Composition Helpers
- Functions: `andThen`, `compose`, `identity`.
- Predicates: `and`, `or`, `negate`.
- Consumers: `andThen`.
- BinaryOperator: `minBy` / `maxBy` with a Comparator.

## Method References vs Lambdas
- Lambdas: `(x) -> x.toUpperCase()`
- Method refs: `String::toUpperCase`, `ClassName::staticMethod`, `instance::method`, `Type::new` (constructor).

## Common Patterns
- Strategy injection: pass `Function`/`Predicate` to customize behavior.
- Factories/lazy: `Supplier<T>` for deferred creation; `BooleanSupplier` for toggles/feature flags.
- Callbacks/handlers: `Consumer<T>` / `BiConsumer<T,U>` for events/accumulators.
- Streams: `map(Function)`, `filter(Predicate)`, `forEach(Consumer)`, `reduce(BinaryOperator)`, `collect(...)`.
- Comparators: method refs with `Comparator.comparing(Foo::bar)`.

## Checked Exceptions
- Standard interfaces do not declare checked exceptions. Options:
  - Wrap/translate inside the lambda.
  - Define your own `ThrowingFunction` and adapt.

## Immutability & Side Effects
- Prefer pure functions: no shared mutable state, no side effects; improves safety and parallelization.
- Captured locals must be effectively final; avoid capturing mutable state.

## Performance Notes
- Use primitive specializations to avoid boxing in numeric hot paths.
- Lambdas are generally efficient (invokedynamic), but avoid allocating new lambdas in tight loops if capturing.

## Writing Your Own
```java
@FunctionalInterface
interface ThrowingFunction<T,R> {
  R apply(T t) throws Exception;
  default <V> ThrowingFunction<T,V> andThen(ThrowingFunction<? super R,? extends V> after) throws Exception {
    return t -> after.apply(apply(t));
  }
}
```
- Keep it SAM; annotate with `@FunctionalInterface`.
- Provide defaults for composition if useful.

## Gotchas
- Overloading with multiple functional signatures can cause inference ambiguity.
- Be careful capturing `this` in constructors.
- Streams are single-use; but stateless functional instances can be reused.

## When to Use
- Any time you pass behavior as data: transformations, filters, validators, formatters, retry/backoff policies, callbacks, pipeline steps, and stream operations.

