# Tasks: BuildSrc Demo Module Plugin

**Input**: Design documents from `/specs/002-add-demo-plugin/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/,
quickstart.md

**Tests**: Test tasks included where coverage is needed to validate plugin behaviour and guardrails.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish buildSrc plugin scaffolding and metadata so subsequent stories have a stable foundation.

- [X] T001 Configure Kotlin DSL and plugin development dependencies in `buildSrc/build.gradle.kts`
- [X] T002 [P] Create plugin marker file
  `buildSrc/src/main/resources/META-INF/gradle-plugins/olc.game_engine.demo.properties`
- [X] T003 [P] Scaffold plugin package directories with placeholder class in
  `buildSrc/src/main/kotlin/olc/game_engine/DemoModulePlugin.kt`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add shared test infrastructure and utilities required by all user stories before feature implementation
begins.

- [X] T004 Extend `buildSrc/build.gradle.kts` with Gradle TestKit and kotlin.test dependencies
- [X] T005 Create reusable TestKit helper `buildSrc/src/test/kotlin/olc/game_engine/TestProjectBuilder.kt`

**Checkpoint**: Test infrastructure ready—user story development can proceed.

---

## Phase 3: User Story 1 - One-Step Demo Setup (Priority: P1) 🎯 MVP

**Goal**: Allow maintainers to configure a demo module using only an application name and entry point, with default
targets, dependencies, and language settings applied automatically.

**Independent Test**: In a fresh demo module using the plugin, run the standard demo build task and verify a runnable
executable is produced with the correct name and defaults.

### Tests for User Story 1

- [X] T006 [US1] Build minimal TestKit fixture demo under
  `buildSrc/src/test/resources/test-projects/minimal-demo/build.gradle.kts`
- [X] T007 [US1] Write setup validation TestKit `buildSrc/src/test/kotlin/olc/game_engine/DemoModulePluginSetupTest.kt`

### Implementation for User Story 1

- [X] T008 [US1] Implement plugin extension data class in
  `buildSrc/src/main/kotlin/olc/game_engine/DemoModuleExtension.kt`
- [X] T009 [US1] Register extension and configure executable binaries in
  `buildSrc/src/main/kotlin/olc/game_engine/DemoModulePlugin.kt`
- [X] T010 [US1] Apply engine/shared assets dependencies and language opt-ins in
  `buildSrc/src/main/kotlin/olc/game_engine/DemoModulePlugin.kt`

**Checkpoint**: New demos can be created via the plugin and built successfully.

---

## Phase 4: User Story 2 - Consistent Demo Maintenance (Priority: P2)

**Goal**: Migrate representative existing demos to the plugin while preserving behaviour and reducing configuration
drift.

**Independent Test**: After migrating, build and run a selected demo to ensure executable name, entry point, and runtime
behaviour match the pre-migration baseline.

### Implementation for User Story 2

- [X] T011 [P] [US2] Migrate `demos/balls/build.gradle.kts` to use the buildSrc demo plugin configuration
- [X] T012 [P] [US2] Migrate `demos/breakout/build.gradle.kts` to use the buildSrc demo plugin configuration
- [X] T013 [P] [US2] Migrate `demos/fireworks/build.gradle.kts` to use the buildSrc demo plugin configuration
- [X] T014 [US2] Record migration smoke test commands and outcomes in `specs/002-add-demo-plugin/quickstart.md`
- [X] T015 [US2] Update maintainer guidance in `README.md` to reference the new plugin workflow

**Checkpoint**: Migrated demos behave identically after adopting the plugin.

---

## Phase 5: User Story 3 - Guardrail Feedback (Priority: P3)

**Goal**: Provide immediate, actionable feedback when plugin inputs are invalid so maintainers can correct issues before
builds fail.

**Independent Test**: Configure demos with missing or incorrect inputs, trigger project evaluation, and verify the build
fails fast with clear guidance.

### Tests for User Story 3

- [X] T016 [US3] Create invalid configuration fixture at
  `buildSrc/src/test/resources/test-projects/invalid-demo/build.gradle.kts`
- [X] T017 [US3] Write guardrail TestKit `buildSrc/src/test/kotlin/olc/game_engine/DemoModulePluginValidationTest.kt`

### Implementation for User Story 3

- [X] T018 [US3] Implement mandatory field validation in `buildSrc/src/main/kotlin/olc/game_engine/DemoModulePlugin.kt`
- [X] T019 [US3] Detect duplicate application names and unresolved entry points in
  `buildSrc/src/main/kotlin/olc/game_engine/DemoModulePlugin.kt`

**Checkpoint**: Invalid configurations surface clear, early errors.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation and verification tasks spanning multiple user stories.

- [X] T020 [P] Add troubleshooting guidance for plugin validation failures in `docs/MAINTAINERS.md`
- [X] T021 Capture final `./gradlew :demos:balls:assemble` verification log in
  `specs/002-add-demo-plugin/validation.log`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 → Phase 2**: Setup must finish before foundational work.
- **Phase 2 → User Story Phases**: Foundational tasks block all user stories; once complete, US1–US3 may proceed.
- **User Story Ordering**: Prioritise US1 (MVP) first; US2 and US3 can start after Phase 2 but depend on US1 artifacts
  for shared plugin code context.
- **Polish Phase**: Runs after targeted user stories reach their checkpoints.

### User Story Dependencies

- **US1**: Depends only on Phases 1–2.
- **US2**: Depends on US1 delivering the plugin core; otherwise independent.
- **US3**: Builds atop US1 validation hooks; independent from US2.

### Task Dependencies (Highlights)

- T004 depends on T001.
- T005 depends on T004.
- T007 depends on T006.
- T009 depends on T008 and T005.
- T010 depends on T009.
- T014 depends on T011–T013.
- T017 depends on T016.
- T018 depends on T009 and T010.
- T019 depends on T018.
- T021 depends on T011–T014.

## Parallel Execution Opportunities

- T002 and T003 can proceed in parallel after T001 since they touch different files.
- T011–T013 may run concurrently to migrate separate demo modules.
- Within US3, T016 can begin while US2 tasks progress, enabling staggered completion.

## Independent Test Criteria by User Story

- **US1**: Execute `./gradlew :demos:sample_app:assemble` using the plugin-only configuration and confirm the resulting
  binary matches the requested name and entry point.
- **US2**: For each migrated demo, run its assemble task and compare binary metadata to pre-migration baselines to
  confirm parity.
- **US3**: Run TestKit validation cases to ensure missing inputs or bad entry points fail during configuration with
  descriptive errors.

## Suggested MVP Scope

- Complete Phases 1–3 (through User Story 1) to deliver a usable plugin for new demos before tackling migrations or
  guardrails.

## Implementation Strategy

1. Finish Setup and Foundational phases to establish plugin scaffolding and test harnesses.
2. Deliver User Story 1 end-to-end (extension, plugin logic, tests) as the MVP.
3. Tackle User Story 2 migrations next, verifying each demo before updating documentation.
4. Implement User Story 3 validation guardrails last to harden the plugin.
5. Conclude with polish tasks to document troubleshooting steps and capture verification logs.
