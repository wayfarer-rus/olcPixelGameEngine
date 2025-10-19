# Implementation Plan: Balls Demo Slider Enhancements

**Branch**: `004-add-ball-sliders` | **Date**: 2025-10-19 | **Spec
**: [/Users/wayfarer/workspace/GIT/olcGameEnginePort/specs/004-add-ball-sliders/spec.md](/Users/wayfarer/workspace/GIT/olcGameEnginePort/specs/004-add-ball-sliders/spec.md)
**Input**: Feature specification from `/specs/004-add-ball-sliders/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the
execution workflow.

## Summary

Add two accessible, labeled sliders to the Balls demo so users can adjust the square body resolution and cannonball
mass, keeping the demo responsive (~60 fps) while reflecting new settings in the existing Kotlin/Native rendering loop.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Kotlin/Native 1.3.71  
**Primary Dependencies**: olc game engine port (engine module), GLFW bindings, kotlinx collections  
**Storage**: N/A  
**Testing**: kotlin.test (native)  
**Target Platform**: Desktop (macOS/Linux/Windows) via GLFW windowing  
**Project Type**: Native desktop demo  
**Performance Goals**: Maintain ~60 fps while adjusting sliders and rebuilding bodies  
**Constraints**: Slider updates apply within 1s; UI colors meet WCAG AA contrast; no regressions to existing demo
controls  
**Scale/Scope**: Single Balls demo module with shared engine reuse

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file currently contains placeholder sections without enforceable principles. No gates to evaluate;
proceed with standard simplicity and accessibility expectations.

**Post-Phase-1 Review**: Research, data model, and contracts stay within the existing simplicity/accessibility intent;
no additional gates triggered.

## Project Structure

### Documentation (this feature)

```
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```
demos/
├── balls/
│   └── src/
│       └── nativeMain/
│           ├── kotlin/                # Demo logic & UI controls
│           └── resources/             # Demo-specific assets
├── shared-assets/
│   └── src/nativeMain/resources/      # Shared textures and sprites

engine/
├── src/
│   ├── nativeMain/kotlin/olc/         # Reusable engine library
│   └── nativeTest/kotlin/             # Engine-level tests
└── src/nativeInterop/cinterop/        # GLFW definitions

games/
└── pixel_shooter/
    └── src/nativeMain/kotlin/         # Reference game using same engine
```

**Structure Decision**: Feature work stays inside `demos/balls/src/nativeMain/kotlin`, leveraging engine primitives from
`engine/src/nativeMain/kotlin/olc` and shared assets as needed; no new modules required.

## Complexity Tracking

*Fill ONLY if Constitution Check has violations that must be justified*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
