# Research Summary

## Decision: Gradle multi-module conversion strategy

- **Decision**: Convert the existing single Kotlin/Native module into a Gradle multi-module project with `engine`,
  `demos:*`, and `games:pixel_shooter` modules registered in `settings.gradle.kts`.
- **Rationale**: Aligns with Kotlin Multiplatform best practices for reusability, enables targeted builds, and keeps
  shared code centralized while avoiding artifact publication.
- **Alternatives considered**:
    - Retain single module with source-set refactoring — rejected because binaries would still compile from one module,
      preventing isolation.
    - Publish the engine to a local Maven repository — rejected per requirement to avoid publication and to minimize
      build pipeline changes.

## Decision: Engine library packaging

- **Decision**: Package the engine as a Kotlin/Native library (`konan_libray`) using the new `engine` module with
  `native` targets matching host platforms.
- **Rationale**: Allows demos and games to consume compiled engine headers/bitcode without duplicated sources while
  working fully offline.
- **Alternatives considered**:
    - Keep engine as source dependency via composite builds — rejected because it blurs module boundaries and
      complicates future sharing beyond the repo.
    - Convert engine to a common shared module with expect/actual splits — unnecessary overhead since current scope
      targets host platforms only.

## Decision: Demo and game dependency pattern

- **Decision**: Each demo/game module will apply the Kotlin Multiplatform plugin, declare only the executable binary,
  and add `implementation(project(":engine"))`.
- **Rationale**: Maintains consistent dependency declarations, ensures refactors of engine propagate automatically, and
  upholds the “local dependency only” constraint.
- **Alternatives considered**:
    - Using `api` dependencies for engine — rejected because consumers should not re-export engine internals.
    - Sharing demo utilities via another library — deferred until a concrete need appears to avoid premature
      abstraction.

## Decision: Shared assets management

- **Decision**: Keep shared art/sprite assets in a dedicated source-set resources directory (e.g.,
  `demos/shared-assets/src/nativeMain/resources`) and reference via Gradle resource dependencies.
- **Rationale**: Prevents duplication across modules and keeps asset loading paths stable.
- **Alternatives considered**:
    - Copy assets into each module — rejected to avoid drift and bloated repository size.
    - Store assets in engine resources — rejected to keep engine agnostic of demo-specific content.

## Decision: Tooling & documentation updates

- **Decision**: Update Gradle tasks, wrapper scripts, and README/quickstart instructions to reflect new module paths and
  binaries.
- **Rationale**: Ensures contributors can build targeted modules post-refactor without confusion and satisfies FR-006
  documentation requirement.
- **Alternatives considered**:
    - Rely solely on Gradle task auto-discovery — rejected because discoverability alone does not meet onboarding
      expectations.

## Build Verification

- `./gradlew :engine:assemble` (passes locally; warnings about deprecated inline classes and native access)
- Follow-up executed: `./gradlew :demos:sample_app:runSampleAppReleaseExecutableMacosX64` and
  `./gradlew :games:pixel_shooter:runPixelShooterGameReleaseExecutableMacosX64` built and linked; run steps skipped by
  Gradle due to headless environment.

