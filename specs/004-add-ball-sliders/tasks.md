# Tasks: Balls Demo Slider Enhancements

**Input**: Design documents from `/specs/004-add-ball-sliders/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: No automated test tasks requested; manual verification documented in docs.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare reference material for QA and contract consumers.

- [X] T001 Create slider enhancement overview covering ranges, QA steps, and accessibility evidence placeholders in
  docs/demos/balls/slider-enhancements.md
- [X] T002 [P] Add control contract quick reference describing `/demos/balls/settings` usage in
  specs/004-add-ball-sliders/contracts/README.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish shared configuration objects consumed by all new sliders.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T003 Create slider configuration models (StructureConfiguration, ProjectileSettings, ControlPresentation) in
  demos/balls/src/nativeMain/kotlin/demos/balls/SliderConfig.kt
- [X] T004 Integrate SliderConfig state into demo lifecycle (initialization, reset) in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt

**Checkpoint**: Foundation ready - user story implementation can now begin.

---

## Phase 3: User Story 1 - Resize the target body (Priority: P1) 🎯 MVP

**Goal**: Allow players to resize the square body via a labeled Structure Size slider with immediate visual feedback.

**Independent Test**: Drag the slider to minimum and maximum stops and confirm the body rebuilds to the selected
dimensions while remaining centered.

### Implementation for User Story 1

- [X] T005 [US1] Instantiate the Structure Size slider using StructureConfiguration defaults in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T006 [US1] Rebuild Body instances based on currentSize and queue updates during active shots in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T007 [US1] Render structure size labels and value badge (e.g., `12×12`) near the slider in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T008 [P] [US1] Document manual verification for structure size extremes in docs/demos/balls/slider-enhancements.md

**Checkpoint**: User Story 1 delivers the MVP; slider-driven body resizing works independently.

---

## Phase 4: User Story 2 - Tune projectile impact (Priority: P2)

**Goal**: Give players control over cannonball mass to observe different impact outcomes.

**Independent Test**: Fire shots at the same structure with mass set to lowest and highest values; heavier shots should
cause visibly more damage.

### Implementation for User Story 2

- [X] T009 [US2] Instantiate the Cannonball Mass slider with ProjectileSettings ranges in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T010 [US2] Apply current mass multiplier when spawning projectiles and computing impact physics in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T011 [US2] Display cannonball mass value badge (percent) alongside slider and default marker in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T012 [P] [US2] Extend manual test plan with heavy vs light impact observations in
  docs/demos/balls/slider-enhancements.md

**Checkpoint**: User Stories 1 and 2 now function independently and can be demoed together.

---

## Phase 5: User Story 3 - Understand the controls instantly (Priority: P3)

**Goal**: Provide accessible, clearly labeled sliders with distinct color coding.

**Independent Test**: Present the control panel for three seconds; viewers can identify each slider’s purpose, and
colors pass WCAG AA contrast checks.

### Implementation for User Story 3

- [X] T013 [US3] Define WCAG-compliant slider color tokens and label strings in
  demos/balls/src/nativeMain/kotlin/demos/balls/ControlPalette.kt
- [X] T014 [US3] Apply palette colors, labels, and default markers when drawing sliders in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T015 [P] [US3] Capture accessibility notes and contrast measurements in docs/demos/balls/slider-enhancements.md

**Checkpoint**: All user stories are independently functional with accessible UI cues.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation and verification updates across the feature.

- [X] T016 Update specs/004-add-ball-sliders/quickstart.md with final verification steps and screenshot instructions
- [X] T017 [P] Record final QA outcomes (default indicators, contrast screenshots, contract linkage) in
  docs/demos/balls/slider-enhancements.md

---

## Dependencies & Execution Order

- **Setup (Phase 1)**: No prerequisites; prepares documentation for later phases.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user story work.
- **User Story Phases (3–5)**: Each depends on Foundational. Stories can run sequentially by priority (US1 → US2 → US3)
  or in parallel once shared state exists.
- **Polish (Phase 6)**: Executes after desired user stories complete to consolidate documentation and verification
  artifacts.

## User Story Dependency Graph

1. **US1 (P1)** → unlocks MVP and provides StructureConfiguration usage.
2. **US2 (P2)** depends on SliderConfig base but not on US1 implementation details.
3. **US3 (P3)** layers accessible presentation on top of completed sliders from US1 and US2.

## Parallel Execution Opportunities

- Documentation tasks marked [P] (T002, T008, T012, T015, T017) can run alongside coding efforts.
- Within coding phases, UI rendering tasks (e.g., T007, T011, T014) can proceed once slider instantiation tasks in the
  same story are ready.
- Different user stories may be developed concurrently after Phase 2, provided coordination on shared files (
  BallsDemo.kt).

## Implementation Strategy

1. Complete Phase 1–2 to establish configuration scaffolding.
2. Deliver MVP by finishing all US1 tasks; verify body resizing works end-to-end.
3. Layer in US2 mass control and validate impact variance.
4. Finalize accessibility improvements in US3.
5. Close with Polish tasks to ensure documentation and quickstart instructions reflect the completed feature.
