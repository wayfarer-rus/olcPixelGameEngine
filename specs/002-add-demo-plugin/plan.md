# Implementation Plan: BuildSrc Demo Module Plugin

**Branch**: `002-add-demo-plugin` | **Date**: 2025-10-19 | **Spec
**: [/Users/wayfarer/workspace/GIT/olcGameEnginePort/specs/002-add-demo-plugin/spec.md](spec.md)
**Input**: Feature specification from `/specs/002-add-demo-plugin/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the
execution workflow.

## Summary

Deliver a reusable Gradle Kotlin DSL plugin inside `buildSrc` that standardises demo module setup by accepting only an
application display name and entry point. The plugin will auto-wire Kotlin/Native targets for supported desktop hosts,
attach engine/shared asset dependencies, enforce language defaults, and surface validation guardrails so maintainers can
spin up or migrate demos quickly without duplicating build logic.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Kotlin 2.3.0-Beta1 (Gradle Kotlin DSL)  
**Primary Dependencies**: Gradle Kotlin Multiplatform plugin, Kotlin/Native host target presets, buildSrc plugin
framework  
**Storage**: N/A  
**Testing**: Gradle TestKit with Kotlin test assertions (to be added)  
**Target Platform**: Desktop build hosts (macOS, Linux, Windows) producing Kotlin/Native demos  
**Project Type**: Multimodule Kotlin/Native repository with buildSrc plugin extension  
**Performance Goals**: Demo setup completed in <10 minutes; migrations retain existing demo runtime behaviour; plugin
validation halts misconfigurations before compilation  
**Constraints**: Must support current desktop host detection logic, enforce shared language opt-ins, and fail fast on
invalid inputs  
**Scale/Scope**: Dozens of demo modules reusing shared engine and assets, with room for additional demos over time

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Constitution file lacks ratified principles; no explicit gating rules provided.
- Default governance: proceed while ensuring plan documents testing approach, avoids unnecessary complexity, and
  documents assumptions.
- Gate Status (initial): PASS (no conflicts detected).
- Post-Phase 1 Review: Outputs (research/data-model/contracts/quickstart) align with scope and maintain simplicity; gate
  remains PASS.

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

```
buildSrc/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── olc/
│   │           └── game_engine/
│   │               └── DemoModulePlugin.kt      # new plugin entry point
│   └── test/
│       └── kotlin/
│           └── olc/
│               └── game_engine/
│                   └── DemoModulePluginTest.kt  # TestKit coverage
demos/
├── <existing demo modules>                      # migrate incrementally to plugin
└── shared-assets/
engine/
└── src/
    └── nativeMain/
        └── kotlin/
            └── olc/
                └── ...                          # shared engine code
```

**Structure Decision**: Introduce a buildSrc-based Gradle plugin under `buildSrc/src/main/kotlin/olc/game_engine` with
accompanying TestKit tests; existing demos remain in `demos/` and will gradually adopt the plugin without restructuring
engine or game modules.

## Complexity Tracking

*Fill ONLY if Constitution Check has violations that must be justified*

| Violation                  | Why Needed         | Simpler Alternative Rejected Because |
|----------------------------|--------------------|--------------------------------------|
| [e.g., 4th project]        | [current need]     | [why 3 projects insufficient]        |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient]  |
