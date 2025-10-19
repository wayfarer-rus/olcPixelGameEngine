# Quickstart: BuildSrc Demo Module Plugin

1. **Create plugin scaffolding**
    - Add `buildSrc` module if absent and define `olc.game_engine.demo` plugin implementation class under
      `buildSrc/src/main/kotlin/`.
    - Provide Gradle plugin metadata (`resources/META-INF/gradle-plugins/`).

2. **Implement plugin logic**
    - Register a typed extension with `applicationName`, `entryPoint`, and optional hooks for extra
      dependencies/resources.
    - Apply Kotlin Multiplatform plugin, configure host target detection, and wire default dependencies/language
      settings.
    - Validate inputs during configuration; throw descriptive exceptions for missing or invalid data.

3. **Add automated coverage**
    - Write Gradle TestKit tests under `buildSrc/src/test/kotlin/` to cover successful demo creation, migration parity,
      and validation failures.
    - Ensure fixtures include sample demo modules referencing the plugin.

4. **Document and migrate**
    - Update demo README or contributor docs with instructions for using the plugin.
    - Convert at least three representative demos to the plugin configuration and confirm smoke tests.

5. **Verification checklist**
    - `./gradlew :demos:<demo>:assemble` succeeds using plugin-only configuration.
    - Validation errors appear immediately when inputs are missing or incorrect.
    - Shared dependencies (engine + shared assets) and language settings are present without manual duplication.

### Migration Smoke Tests

| Demo      | Command                               | Expected Outcome                                                                        |
|-----------|---------------------------------------|-----------------------------------------------------------------------------------------|
| Balls     | `./gradlew :demos:balls:assemble`     | Builds native executable named `BallsDemo` with entry point `demos.balls.main`.         |
| Breakout  | `./gradlew :demos:breakout:assemble`  | Builds native executable named `BreakoutDemo` with entry point `demos.breakout.main`.   |
| Fireworks | `./gradlew :demos:fireworks:assemble` | Builds native executable named `FireworksDemo` with entry point `demos.fireworks.main`. |
