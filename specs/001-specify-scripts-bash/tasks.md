# Tasks: Modular Engine Module Split

**Input**: Design documents from `/specs/001-specify-scripts-bash/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: No new automated tests requested beyond migrating existing ones; add targeted checks only where noted.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare repository layout for the upcoming multi-module refactor.

- [X] T001 Create base module directories (`engine/src/nativeMain/{kotlin,resources}`, `demos/`, `games/`) per plan in
  repository root.
- [X] T002 Create shared assets scaffold at `demos/shared-assets/src/nativeMain/resources/.keep` for reusable demo
  resources.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core Gradle configuration that MUST be complete before ANY user story can be implemented.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T003 Update `settings.gradle.kts` to enable hierarchical project names and include modules `:engine`,
  `:demos:<name>`, and `:games:pixel_shooter`.
- [X] T004 Rewrite root `build.gradle.kts` to use the Kotlin Multiplatform plugin in subprojects (apply false),
  configure shared repositories, and remove the legacy single-module target definitions.

**Checkpoint**: Foundation ready — user story implementation can now begin in parallel.

---

## Phase 3: User Story 1 - Package the core engine once (Priority: P1) 🎯 MVP

**Goal**: Expose the core engine as a standalone reusable library module without publishing artifacts externally.

**Independent Test**: `./gradlew :engine:assemble` succeeds from a clean checkout and produces the library artifact
without referencing demo or game sources.

### Implementation for User Story 1

- [X] T005 [P] [US1] Move engine API sources from `src/olcGameEnginePortMain/kotlin/olc` to
  `engine/src/nativeMain/kotlin/olc`.
- [X] T006 [P] [US1] Move shared math and utility code from `src/olcGameEnginePortMain/kotlin/geometry_2d` to
  `engine/src/nativeMain/kotlin/geometry_2d`.
- [X] T007 [US1] Configure `engine/build.gradle.kts` as a Kotlin/Native library with host targets, GLFW cinterop (
  `src/nativeInterop/cinterop/libglfw3.def`), and
  `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")`.
- [X] T008 [US1] Relocate engine resources from `src/olcGameEnginePortMain/resources` to
  `engine/src/nativeMain/resources`.
- [X] T009 [US1] Move engine unit tests from `src/olcGameEnginePortTest/kotlin/olc` to
  `engine/src/nativeTest/kotlin/olc` and wire the test source set in `engine/build.gradle.kts`.
- [X] T010 [US1] Update `README.md` and `specs/001-specify-scripts-bash/quickstart.md` with the new engine build
  instructions referencing `./gradlew :engine:assemble`.

**Checkpoint**: Engine library builds independently and serves as the single dependency point.

---

## Phase 4: User Story 2 - Manage demos independently (Priority: P2)

**Goal**: Place every demo in its own module under `demos/*`, each consuming the engine library via local project
dependency.

**Independent Test**: `./gradlew :demos:<demo>:runReleaseExecutable` succeeds for each migrated demo using only declared
dependencies.

### Implementation for User Story 2

- [X] T011 [P] [US2] Create `demos/fireworks/build.gradle.kts` with Kotlin/Native executable depending on
  `project(":engine")` and move sources from `src/olcGameEnginePortMain/kotlin/demos/fireworks` to
  `demos/fireworks/src/nativeMain/kotlin/demos/fireworks`.
- [X] T012 [P] [US2] Create `demos/asteroids/build.gradle.kts` and relocate
  `src/olcGameEnginePortMain/kotlin/demos/asteroids` into `demos/asteroids/src/nativeMain/kotlin/demos/asteroids`.
- [X] T013 [P] [US2] Create `demos/breakout/build.gradle.kts` and move `src/olcGameEnginePortMain/kotlin/demos/breakout`
  to `demos/breakout/src/nativeMain/kotlin/demos/breakout`.
- [X] T014 [P] [US2] Create `demos/boids/build.gradle.kts` and move `src/olcGameEnginePortMain/kotlin/demos/boids` to
  `demos/boids/src/nativeMain/kotlin/demos/boids`.
- [X] T015 [P] [US2] Create `demos/destructible_sprite/build.gradle.kts` and move
  `src/olcGameEnginePortMain/kotlin/demos/destructible_sprite` to
  `demos/destructible_sprite/src/nativeMain/kotlin/demos/destructible_sprite`.
- [X] T016 [P] [US2] Create `demos/balls/build.gradle.kts` and move `src/olcGameEnginePortMain/kotlin/demos/balls` to
  `demos/balls/src/nativeMain/kotlin/demos/balls`.
- [X] T017 [P] [US2] Create `demos/mandelbrot/build.gradle.kts` and move
  `src/olcGameEnginePortMain/kotlin/demos/mandelbrot` to `demos/mandelbrot/src/nativeMain/kotlin/demos/mandelbrot`.
- [X] T018 [P] [US2] Create `demos/bejewelled/build.gradle.kts`, move
  `src/olcGameEnginePortMain/kotlin/demos/bejewelled_maybe` to
  `demos/bejewelled/src/nativeMain/kotlin/demos/bejewelled`, and relocate `src/olcGameEnginePortMain/resources/jewels`
  to `demos/bejewelled/src/nativeMain/resources/jewels`.
- [X] T019 [P] [US2] Create `demos/sample_app/build.gradle.kts`, move
  `src/olcGameEnginePortMain/kotlin/sample/SampleApp.kt` to
  `demos/sample_app/src/nativeMain/kotlin/sample/SampleApp.kt`, and wire its executable entry point.
- [X] T020 [P] [US2] Create `demos/dungeon_warping/build.gradle.kts`, move
  `src/olcGameEnginePortMain/kotlin/sample/DungeonWarping.kt` to
  `demos/dungeon_warping/src/nativeMain/kotlin/sample/DungeonWarping.kt`, and relocate
  `src/olcGameEnginePortMain/resources/DungeonWarping` to
  `demos/dungeon_warping/src/nativeMain/resources/DungeonWarping`.
- [X] T021 [US2] Update `demos/shared-assets/src/nativeMain/resources` with any remaining shared demo resources and
  document usage in `demos/shared-assets/README.md`.
- [X] T022 [US2] Refresh `quickstart.md` demo commands to reference `:demos:<name>:runReleaseExecutable` targets for
  every migrated module.

**Checkpoint**: Each demo builds and runs via its module with the shared engine dependency.

---

## Phase 5: User Story 3 - Isolate the pixel shooter game (Priority: P3)

**Goal**: Host the pixel shooter game in a dedicated module under `games/` reusing the engine library.

**Independent Test**: `./gradlew :games:pixel_shooter:runReleaseExecutable` launches the game using only local project
dependencies.

### Implementation for User Story 3

- [X] T023 [P] [US3] Create `games/pixel_shooter/build.gradle.kts` and move sources from
  `src/olcGameEnginePortMain/kotlin/game/pixel_shooter` to
  `games/pixel_shooter/src/nativeMain/kotlin/game/pixel_shooter`.
- [X] T024 [P] [US3] Relocate any pixel shooter resources from `src/olcGameEnginePortMain/resources` to
  `games/pixel_shooter/src/nativeMain/resources`.
- [X] T025 [US3] Update pixel shooter Gradle binaries to expose the executable entry point and depend on
  `project(":engine")`.
- [X] T026 [US3] Document pixel shooter run instructions in `README.md` referencing
  `:games:pixel_shooter:runReleaseExecutable`.

**Checkpoint**: Pixel shooter operates independently of demo modules.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Repository cleanup and verification across modules.

- [X] T027 Remove obsolete directories (`src/olcGameEnginePortMain`, `src/olcGameEnginePortTest`) now migrated into
  modules.
- [X] T028 Update `README.md` architecture section to reflect engine/demos/games module layout diagram.
- [X] T029 Run full regression `./gradlew :engine:assemble` and capture follow-up run requirements in
  `specs/001-specify-scripts-bash/research.md`.
- [X] T030 Verify quickstart commands end-to-end and append confirmation checklist to
  `specs/001-specify-scripts-bash/quickstart.md`.

---

## Dependencies & Execution Order

- **Phase Dependencies**
    - Setup (Phase 1): No dependencies.
    - Foundational (Phase 2): Depends on Phase 1; blocks all user stories.
    - User Story phases (3–5): Each depends on completion of Phase 2; may proceed in parallel once the engine module
      exists.
    - Polish (Phase 6): Depends on desired user stories being complete.

- **User Story Dependencies**
    - User Story 1 (P1): Starts after Foundational; no dependency on other stories.
    - User Story 2 (P2): Requires User Story 1 completion to consume the engine library.
    - User Story 3 (P3): Requires User Story 1 completion; independent of User Story 2 content.

---

## Parallel Execution Examples

- **User Story 1**: Tasks T005 and T006 may run in parallel because they touch different source directories.
- **User Story 2**: Module creation tasks T011–T020 can be distributed across contributors since each targets unique
  module paths.
- **User Story 3**: Tasks T023 and T024 may run concurrently; T025 follows after build file creation.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phases 1–2.
2. Deliver User Story 1 (engine library) and validate via `./gradlew :engine:assemble`.
3. Pause for stakeholder review before proceeding to other stories.

### Incremental Delivery

1. Complete Setup and Foundational phases.
2. Implement User Story 1 (engine library) → validate.
3. Implement User Story 2 (demo modules) → validate each demo.
4. Implement User Story 3 (pixel shooter module) → validate.

### Parallel Team Strategy

1. Team finalizes Setup and Foundational together.
2. Assign User Story 1 to establish the engine library.
3. Once engine is ready, separate contributors handle User Story 2 modules in parallel while another completes User
   Story 3.
