# Implementation Plan: Balls Demo Toughness Slider

**Branch**: `003-add-toughness-slider` | **Date**: 2025-10-19 | **Spec**: specs/003-add-toughness-slider/spec.md  
**Input**: Feature specification from `/specs/003-add-toughness-slider/spec.md`

## Summary

Add a toughness slider to the Balls demo so viewers can tune the central body’s resistance between 0 and 100 in real
time by reusing the existing slider UI module and feeding the selected value into the simulation parameters.

## Technical Context

**Language/Version**: Kotlin/Native 1.3.71  
**Primary Dependencies**: `olc.game_engine` core library, slider utilities from `:demos:slider`  
**Storage**: N/A – in-memory simulation only  
**Testing**: kotlin.test (native)  
**Target Platform**: Desktop Kotlin/Native demos (macOS/Linux/Windows)  
**Project Type**: Kotlin/Native multi-module demo suite  
**Performance Goals**: Maintain smooth playback with at most one hitch >0.1s during slider interaction (per SC-003)  
**Constraints**: Slider updates must apply instantly without restarting the simulation and should reuse existing demo
visuals  
**Scale/Scope**: Single Balls demo interaction affecting session-only state

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Gate Status: **PASS** – `.specify/memory/constitution.md` currently contains placeholder text and no enforceable
principles, so there are no blocking gates for this feature.

## Project Structure

### Documentation (this feature)

```
specs/003-add-toughness-slider/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```
engine/
├── build.gradle.kts
├── src/nativeMain/kotlin/olc/
├── src/nativeTest/kotlin/
└── src/nativeInterop/cinterop/
demos/
├── balls/src/nativeMain/kotlin/
├── slider/src/nativeMain/kotlin/
└── shared-assets/src/nativeMain/resources/
games/
└── pixel_shooter/src/nativeMain/kotlin/
gradle/
└── wrapper/
```

**Structure Decision**: Continue using the existing Kotlin/Native multi-module layout; implementation will primarily
touch `demos/balls` while reusing UI helpers from `demos/slider` and assets already managed in `demos/shared-assets`.

## Complexity Tracking

No constitution violations identified.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|

## Implementation Notes (Post-Plan)

- Toughness slider now throttles energy transfer down to roughly 3% and raises break thresholds up to 35×, with the top
  of the scale (slider 100) matching the earlier toughness felt at position 60.
- Glancing impacts scale up to 6× the baseline requirement, further reducing incidental deformation at tougher settings.
- Resetting the demo (keyboard `R`) realigns both the slider position and the displayed numeric label to the default 0
  state.
- Future iteration idea: surface the multiplier bounds in UI tooling so designers can experiment without touching
  physics constants.
