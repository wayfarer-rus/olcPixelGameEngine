# Implementation Plan: Modular Engine Module Split

**Branch**: `001-specify-scripts-bash` | **Date**: 2025-10-18 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-specify-scripts-bash/spec.md`

## Summary

Refactor the Kotlin/Native repository into a multi-module build: promote the core engine into a reusable library,
relocate each demo under its own module within a `demos` grouping, separate the pixel shooter game under `games`, and
ensure every runnable artifact depends on the shared engine through local project references only.

## Technical Context

**Language/Version**: Kotlin/Native (Kotlin Multiplatform plugin 2.3.0-Beta1)  
**Primary Dependencies**: kotlinx-coroutines-core 1.9.0, GLFW native headers via cinterop  
**Storage**: N/A (in-memory assets only)  
**Testing**: kotlin.test host tests executed via `./gradlew check`  
**Target Platform**: Desktop hosts (macOS x64, Linux x64, Windows x64)  
**Project Type**: Multi-module Kotlin/Native application with demos/games executables  
**Performance Goals**: Maintain pre-refactor runtime behavior; keep engine-only build under 5 minutes on standard dev
hardware  
**Constraints**: Local-only project dependencies; no publishing of engine artifacts; retain existing Gradle wrapper
usage  
**Scale/Scope**: Existing engine plus ~10 demo executables and one pixel shooter game executable

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file is placeholder-only and defines no active principles or gates. No compliance blockers identified
for this plan cycle.

**Post-Phase 1 Review**: Design artifacts (research, data model, contracts, quickstart) remain aligned with the
absent-gate constitution; no new violations introduced.

## Project Structure

### Documentation (this feature)

```
specs/001-specify-scripts-bash/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```
build.gradle.kts
settings.gradle.kts
engine/
├── build.gradle.kts
├── src/
│   └── nativeMain/kotlin/olc/game_engine/
└── resources/

demos/
├── fireworks/
│   ├── build.gradle.kts
│   └── src/nativeMain/kotlin/demos/fireworks/
├── asteroids/
│   └── ...
├── breakout/
│   └── ...
└── shared-assets/

games/
└── pixel_shooter/
    ├── build.gradle.kts
    └── src/nativeMain/kotlin/games/pixel_shooter/

gradle/
└── wrapper/

src/nativeInterop/cinterop/
└── libglfw3.def
```

**Structure Decision**: Adopt a Gradle multi-module layout rooted at the project level: one reusable `engine` library,
per-demo modules under `demos/*`, and game modules under `games/*`, all registered in `settings.gradle.kts` and
consuming the engine via `implementation(project(":engine"))`.

## Complexity Tracking

No constitution violations recorded; complexity tracking not required.
