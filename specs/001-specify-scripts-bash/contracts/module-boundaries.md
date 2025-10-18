# Module Dependency Contract

This feature does not expose external HTTP or IPC APIs. Instead, it defines internal module boundaries that must be
enforced inside the Gradle multi-module project.

| Module                | Type                     | Consumes                                              | Exposes                                                         |
|-----------------------|--------------------------|-------------------------------------------------------|-----------------------------------------------------------------|
| `engine`              | Kotlin/Native library    | Kotlin stdlib, kotlinx-coroutines-core, GLFW cinterop | Public engine API surface (classes, functions, data structures) |
| `demos:<name>`        | Kotlin/Native executable | `engine`, demo-specific assets                        | Executable entry point only                                     |
| `games:pixel_shooter` | Kotlin/Native executable | `engine`, pixel shooter assets                        | Executable entry point only                                     |

## Contract Rules

1. Executable modules MUST declare `implementation(project(":engine"))` and MUST NOT access engine source directories
   directly.
2. Executable modules MUST avoid inter-module dependencies (no demo-to-demo or demo-to-game coupling).
3. Shared assets SHOULD be imported via resource dependencies; duplication across modules is considered a contract
   breach.
