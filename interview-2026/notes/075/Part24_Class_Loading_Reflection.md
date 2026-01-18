# Part 24: Class Loading & Reflection - Quick Revision

## ClassLoader Hierarchy

- **Bootstrap ClassLoader**: Loads core Java classes (rt.jar)
- **Extension ClassLoader**: Loads extension classes
- **Application ClassLoader**: Loads application classes from classpath
- **Custom ClassLoader**: User-defined class loaders

## Delegation Model

- **Parent-First**: Child ClassLoader delegates to parent first
- **Class Loading Phases**: Loading → Linking (verification, preparation, resolution) → Initialization
- **Class Loading Isolation**: Different ClassLoaders can load same class, different Class objects

## Reflection API

- **Class**: Get class information
- **Method**: Invoke methods dynamically
- **Field**: Access/modify fields
- **Constructor**: Create instances dynamically
- **Performance**: Reflection is slower, cache when possible

## Annotations

- **Retention**: SOURCE (compile-time), CLASS (class file), RUNTIME (available at runtime)
- **Target**: Where annotation can be used (TYPE, METHOD, FIELD, etc.)
- **Processing**: Compile-time (annotation processors) or runtime (reflection)

## Use Cases

- **Frameworks**: Spring (dependency injection), Hibernate (ORM)
- **Testing**: JUnit, Mockito
- **Code Generation**: Annotation processors, dynamic code generation
