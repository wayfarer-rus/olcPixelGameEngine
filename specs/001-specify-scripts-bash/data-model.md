# Domain Model

## Modules

### Engine Library (`engine`)

- **Purpose**: Hosts shared rendering, input, and timing functionality.
- **Key Components**: Core engine classes, resource management helpers, math utilities.
- **Dependencies**: Kotlin/Native stdlib, kotlinx-coroutines-core, GLFW cinterop.
- **Exposes**: Public API surface packaged as a Kotlin/Native library consumed by downstream modules.
- **State Considerations**: Pure library — no persisted state; runtime state managed by consumers.

### Demo Modules (`demos:*`)

- **Purpose**: Showcase focused scenarios powered by the engine (e.g., fireworks, asteroids, breakout).
- **Dependencies**: `implementation(project(":engine"))`, module-specific assets/resources.
- **Inputs**: Engine API, shared assets (where needed), demo-specific configuration.
- **Outputs**: Executable binaries named per demo module.
- **State Considerations**: Runtime-only state; no cross-module data sharing.

### Game Modules (`games:pixel_shooter`)

- **Purpose**: Ship full game experiences with independent roadmaps.
- **Dependencies**: `implementation(project(":engine"))`, dedicated asset bundle.
- **Inputs**: Engine API, game configuration, art/audio assets.
- **Outputs**: Executable binary for the pixel shooter game.
- **State Considerations**: Maintains game state internally; no persistence beyond current behavior.

## Relationships

- `engine` is the sole shared library; all runnable modules depend on it through local project references.
- Demo modules are siblings — no demo depends on another demo.
- Game modules follow the same dependency pattern as demos but live under a separate top-level grouping for clarity.

## Validation & Constraints

- Every executable module must compile without directly referencing engine source directories.
- Shared assets must live in dedicated resource directories and be linked via Gradle resource configuration.
- Adding a new module requires registering it in `settings.gradle.kts` and declaring the engine dependency.
