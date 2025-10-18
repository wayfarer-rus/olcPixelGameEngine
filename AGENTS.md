# Repository Guidelines

## Project Structure & Module Organization

Core Kotlin/Native engine sources live in `src/olcGameEnginePortMain/kotlin`, with demo entry points grouped under
`demos.*`. Shared assets ship from `src/olcGameEnginePortMain/resources`. Tests and sample sprites reside in
`src/olcGameEnginePortTest/{kotlin,resources}`. Native interop definitions stay inside `src/nativeInterop/cinterop`; add
platform headers or `.def` files there so Gradle can wire GLFW correctly. Gradle builds write artifacts to `build/`,
with host executables under `build/bin/olcGameEnginePort`.

## Build, Test, and Development Commands

Use the wrapper (`./gradlew`) to pick the right Kotlin/Native toolchain. `./gradlew assemble` compiles the engine and
demos, dropping binaries in `build/bin`. `./gradlew runOlcGameEnginePortSampleAppReleaseExecutableOlcGameEnginePort`
launches the sample app; substitute `FireworksDemo`, `AsteroidsDemo`, `BoidsDemo`, or `BallsDemo` to run other demos.
`./gradlew check` runs the host test suite; add `--info` when triaging native assertion failures. Install GLFW before
building (`brew install glfw` on macOS, `apt install glfw` on Ubuntu, `MINGW64_DIR` on Windows).

## Coding Style & Naming Conventions

Follow idiomatic Kotlin style: four-space indentation, `PascalCase` for classes/objects, `camelCase` for functions and
properties, and uppercase snake case for constants. Keep engine APIs namespaced under `olc.game_engine`. Prefer
immutable `val` properties, and co-locate demo-specific utilities with their entry point package. When using
experimental unsigned types, wrap affected files with `@OptIn(ExperimentalUnsignedTypes::class)` to mirror existing
practice.

## Testing Guidelines

Host tests rely on `kotlin.test` and live in the `olc.game_engine` test package. Write scenario-focused tests (
`SpriteImplTest`, `PixelGameEngineTest`) that exercise draw paths without hitting platform OpenGL. Name tests with
`test<Feature>` and use the in-memory fixtures in `src/olcGameEnginePortTest/resources`. Run them via
`./gradlew check` (or the IDE Kotlin/Native test runner) before pushing.

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
