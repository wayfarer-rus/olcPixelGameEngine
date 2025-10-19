# Maintainer Guide: Demo Module Plugin

This repository ships a buildSrc convention plugin (`olc.game_engine.demo`) that standardises how demo modules are
defined.
Apply it from any `demos/<name>/build.gradle.kts` file and configure the `demoModule` block with your executable
metadata.

## Common Validation Failures

| Message fragment                        | What it means                                                                                                                                                                         | How to fix                                                                                                                     |
|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| `applicationName must not be blank`     | The `demoModule {}` block is missing the `applicationName` value or it resolves to an empty string after trimming.                                                                    | Provide a unique display name for the executable, e.g. `applicationName = "OrbitDemo"`.                                        |
| `entryPoint must not be blank`          | The `entryPoint` property is empty or missing.                                                                                                                                        | Supply the fully qualified `main` function, such as `entryPoint = "demos.orbit.main"`.                                         |
| `Entry point 'X' could not be resolved` | The plugin could not find matching Kotlin sources for the declared entry point. Either the package directory is absent under `src/nativeMain/kotlin` or the function name is missing. | Verify that the path `src/nativeMain/kotlin/<package>` exists and that at least one `.kt` file declares `fun <functionName>(`. |
| `Duplicate demo application name 'X'`   | Another demo already reserves the same `applicationName`. Executables must be uniquely named to avoid collision of output binaries.                                                   | Choose a different `applicationName` or rename the existing demo to free the identifier.                                       |

## Adding Optional Configuration

- `demoModule.dependencies.add(...)` appends extra dependencies to the default engine and shared-assets bundles.
- `demoModule.resourceDirs.add("src/nativeMain/customResources")` registers additional resource folders to package with
  the executable.

After any change, run `./gradlew :demos:<name>:assemble` to make sure the module still builds on your host platform. If
Gradle reports a validation failure, consult the table above before escalating.
