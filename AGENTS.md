# Repository Guidelines

## Project Structure & Module Organization

Core Kotlin/Native engine sources now live in `engine/src/nativeMain/kotlin/olc`, packaged as a reusable library module.
Each demo is isolated under `demos/<name>/src/nativeMain/kotlin`, and the pixel shooter game sits in
`games/pixel_shooter/src/nativeMain/kotlin`. Shared sprite or texture assets belong in the `demos/shared-assets`
module (`src/nativeMain/resources`). Engine tests and fixtures reside in `engine/src/nativeTest/{kotlin,resources}`.
Native interop definitions (e.g., GLFW) ship with the engine library at `engine/src/nativeInterop/cinterop`.
Gradle builds write module-specific artifacts to `build/bin/<module>/`.

## Build, Test, and Development Commands

Use the wrapper (`./gradlew`) to pick the right Kotlin/Native toolchain. `./gradlew assemble` builds every module.
`./gradlew :engine:assemble` compiles the shared engine library. Run demos with
`./gradlew :demos:<name>:run<BinaryName>ReleaseExecutable<HostTarget>` and the pixel shooter with
`./gradlew :games:pixel_shooter:runPixelShooterGameReleaseExecutable<HostTarget>`. Install GLFW before building (
`brew install glfw`
on macOS, `apt install glfw` on Ubuntu, `MINGW64_DIR` on Windows).

## Coding Style & Naming Conventions

Follow idiomatic Kotlin style: four-space indentation, `PascalCase` for classes/objects, `camelCase` for functions and
properties, and uppercase snake case for constants. Keep engine APIs namespaced under `olc.game_engine`. Prefer
immutable `val` properties, and co-locate demo-specific utilities with their entry point package. When using
experimental unsigned types, wrap affected files with `@OptIn(ExperimentalUnsignedTypes::class)` to mirror existing
practice.

## Testing Guidelines

Host tests rely on `kotlin.test` and live in the `olc.game_engine` package under `engine/src/nativeTest/kotlin`. Write
scenario-focused tests (`SpriteImplTest`, `PixelGameEngineTest`) that exercise draw paths without hitting platform
OpenGL. Name tests with `test<Feature>` and use the in-memory fixtures in `engine/src/nativeTest/resources`. Run them
via
`./gradlew :engine:check` (or the IDE Kotlin/Native test runner) before pushing.

## Commit & Pull Request Guidelines

Existing history favors concise, lowercase summaries (e.g., `small balls demo`,
`chore: kotlin and dependecies upgrade`). Use an optional `type:` prefix when it clarifies intent. Commits should be
scoped to one demo or subsystem. Pull requests must outline the change, note platform coverage (macOS/Linux/Windows),
link related issues, and include screenshots or GIFs when altering a demo’s visuals. Mention any new Gradle tasks or
native runtime requirements so reviewers can reproduce results.

## Platform & Dependency Notes

Java 17+ is required for Gradle; install it before running builds. Keep GLFW up to date system-wide and verify
`MINGW64_DIR` points to a valid MinGW path on Windows contributors. If you add new native libraries, declare their
include directories in `build.gradle.kts` under the corresponding `cinterops` block and document any extra setup here.
