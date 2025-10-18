# Quickstart: Modular Engine Project

## Prerequisites

- Java 17+
- Kotlin/Native toolchain via Gradle wrapper (`./gradlew`)
- GLFW installed on host (see README for platform instructions)

## Setup

1. Fetch dependencies: `./gradlew --version` (initializes wrapper).
2. Sync Gradle project in IDE to pick up new modules (`engine`, `demos:*`, `games:pixel_shooter`).

## Build Targets

- Build everything: `./gradlew assemble`
- Build engine library only: `./gradlew :engine:assemble`

### Demo executables

```
./gradlew :demos:fireworks:runFireworksDemoReleaseExecutableMacosX64
./gradlew :demos:asteroids:runAsteroidsDemoReleaseExecutableMacosX64
./gradlew :demos:breakout:runBreakoutDemoReleaseExecutableMacosX64
./gradlew :demos:boids:runBoidsDemoReleaseExecutableMacosX64
./gradlew :demos:destructible_sprite:runDestructibleBlockDemoReleaseExecutableMacosX64
./gradlew :demos:balls:runBallsDemoReleaseExecutableMacosX64
./gradlew :demos:mandelbrot:runMandelbrotDemoReleaseExecutableMacosX64
./gradlew :demos:bejewelled:runBejewelledDemoReleaseExecutableMacosX64
./gradlew :demos:sample_app:runSampleAppReleaseExecutableMacosX64
./gradlew :demos:dungeon_warping:runDungeonWarpingDemoReleaseExecutableMacosX64
```

### Game executables

```
./gradlew :games:pixel_shooter:runPixelShooterGameReleaseExecutableMacosX64
```

## Adding New Modules

1. Create module directory under `demos/` or `games/`.
2. Add `build.gradle.kts` applying the Kotlin Multiplatform plugin with an executable binary.
3. Register the module in `settings.gradle.kts`.
4. Declare `implementation(project(":engine"))` in the new module.

Following these steps keeps all executables aligned with the modular architecture and ensures the shared engine remains
the single point of truth.

## Verification Checklist

- [x] `./gradlew :engine:assemble`
- [x] `./gradlew :demos:sample_app:runSampleAppReleaseExecutableMacosX64` (Gradle run task skipped after linking;
  requires windowing environment to display)
- [x] `./gradlew :games:pixel_shooter:runPixelShooterGameReleaseExecutableMacosX64` (Gradle run task skipped after
  linking; requires windowing environment to display)
