# Tasks: Balls Demo Toughness Slider

**Input**: Design documents from `/specs/003-add-toughness-slider/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Manual verification via quickstart; no dedicated automated test tasks requested in spec.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Ensure the Balls demo can reuse the shared slider UI module.

- [X] T001 Add slider module dependency in demos/balls/build.gradle.kts so the Balls demo can access
  `demos.slider.Slider`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared state objects required by multiple user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 Create toughness control state and mapping utilities in
  demos/balls/src/nativeMain/kotlin/demos/balls/ToughnessControl.kt

**Checkpoint**: Foundation ready — user story implementation can now begin.

---

## Phase 3: User Story 1 – Adjust Toughness In Real Time (Priority: P1) 🎯 MVP

**Goal**: Allow viewers to manipulate a toughness slider that immediately alters the central body’s resistance.

**Independent Test**: Launch the Balls demo, sweep the slider between 0 and 100, and confirm the body’s resistance
tightens and loosens in real time without restarting the simulation.

### Implementation for User Story 1

- [X] T003 [US1] Instantiate the toughness slider and session state in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T004 [US1] Render the slider each frame and sync slider input into the toughness state in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T005 [US1] Apply the toughness multiplier to projectile energy/threshold calculations in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt

**Checkpoint**: User Story 1 functional and testable on its own.

---

## Phase 4: User Story 2 – Understand Toughness Settings (Priority: P2)

**Goal**: Expose clear labeling and numeric feedback so viewers know the active toughness level.

**Independent Test**: Load the Balls demo, observe the toughness label at 0, move the slider to several positions, and
confirm the displayed value updates accurately and resets to 0 when the demo resets.

### Implementation for User Story 2

- [X] T006 [US2] Display a “Toughness” label with 0–100 numeric value in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt
- [X] T007 [US2] Reset slider position and display to default when reset() runs in
  demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt

**Checkpoint**: User Stories 1 and 2 both operate independently and provide complete feedback.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Documentation and validation that span multiple stories.

- [X] T008 Document manual verification steps and expected slider behavior in
  specs/003-add-toughness-slider/quickstart.md
- [X] T009 Record final tuning notes and follow-up considerations in specs/003-add-toughness-slider/plan.md

---

## Dependencies & Execution Order

1. **Phase 1 → Phase 2**: Complete slider dependency wiring (T001) before introducing shared toughness state (T002).
2. **Phase 2 → User Stories**: Toughness state (T002) blocks all story work; once done, User Story 1 (T003–T005) can
   begin.
3. **User Story 1 → User Story 2**: User Story 2 (T006–T007) depends on the slider being functional, so finish US1
   first.
4. **Polish Tasks**: T008–T009 follow after both user stories are complete.

---

## Parallel Opportunities

- After T002, documentation tasks (T008–T009) can draft in parallel with polishing if implementation details are known.
- Within User Story 1, T004 and T005 can proceed in parallel once T003 establishes shared fields, as one focuses on
  rendering and the other on physics calculations.
- User Story 2 tasks T006 and T007 touch separate code paths (rendering vs. reset logic) and can proceed concurrently
  after User Story 1 completes.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (T001) and Phase 2 (T002).
2. Deliver User Story 1 tasks (T003–T005) to achieve a functioning toughness slider with real-time effects.
3. Validate slider behavior manually per the quickstart before continuing.

### Incremental Delivery

1. MVP (User Story 1) provides tangible interaction benefits.
2. Layer User Story 2 (T006–T007) to improve clarity and reset behavior.
3. Finish with cross-cutting documentation (T008–T009) to support adoption and future tuning.

### Parallel Team Strategy

- Developer A: Handle Build + Foundational phases (T001–T002), then focus on slider rendering (T003–T004).
- Developer B: After T003, implement physics adjustments (T005) and begin User Story 2 reset handling (T007).
- Developer C: Once slider visuals exist (T004), add labeling feedback (T006) and maintain documentation updates (
  T008–T009).
