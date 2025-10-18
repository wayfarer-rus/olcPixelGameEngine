# Feature Specification: Modular Engine Module Split

**Feature Branch**: `001-specify-scripts-bash`  
**Created**: 2025-10-18  
**Status**: Draft  
**Input**: User description: "Refactor project. Split inro modules. Make olcGameEnginePort a library. Move all demos
into own modules under demos. Make all demos use olcGameEngine as library dependency. Move pixel_shooter game into own
module under games. Make pixel_shooter use olcGameEngine as a library dependency. We do not want to publish
olcGameEngine library anywhere at that point. Make all dependencies local via implementation(project..."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Package the core engine once (Priority: P1)

A core engine maintainer needs the engine available as a reusable library so that updates can be made in one place while
demos and games keep working.

**Why this priority**: Without a shared library module, every downstream experience breaks when the engine changes,
slowing the entire team.

**Independent Test**: Build only the engine library and confirm another module links against the produced artifact
without duplicating engine sources.

**Acceptance Scenarios**:

1. **Given** the engine sources, **When** the maintainer builds the library module, **Then** a consumable artifact is
   produced without needing to publish externally.
2. **Given** a demo module referencing the library, **When** the maintainer updates engine code, **Then** the demo picks
   up the change through the library dependency without manual copy steps.

---

### User Story 2 - Manage demos independently (Priority: P2)

A demo author wants each demo isolated in its own module tree so they can iterate, test, and ship updates without
affecting other demos.

**Why this priority**: Independent modules enable targeted builds, clearer ownership, and easier onboarding for demo
contributors.

**Independent Test**: Run the build for a single demo module and verify it compiles and executes using only its declared
dependencies.

**Acceptance Scenarios**:

1. **Given** a demo module under the demos grouping, **When** a contributor builds or runs that demo, **Then** the build
   succeeds using the shared engine library with no hidden cross-module references.

---

### User Story 3 - Isolate the pixel shooter game (Priority: P3)

The games team needs the pixel shooter experience decoupled from demos so it can evolve on its own roadmap while reusing
the engine.

**Why this priority**: Separating games from demos clarifies release cadence and prevents demo-specific assumptions from
leaking into the game.

**Independent Test**: Build and run the pixel shooter module to confirm it operates with only the engine library as a
shared dependency.

**Acceptance Scenarios**:

1. **Given** the pixel shooter module under the games grouping, **When** a contributor runs its build, **Then** it links
   solely against the shared engine library and produces the expected executable.

### Edge Cases

- A contributor builds only the engine library without any demo or game modules; the build must complete successfully.
- A demo or game accidentally references engine sources directly; the build should fail or otherwise enforce using the
  shared library.
- A new demo module is added under the demos grouping; project conventions must make the dependency pattern obvious to
  avoid regressions.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The project MUST expose the core engine as a standalone library module that other modules can depend on
  without external publication.
- **FR-002**: Each existing demo MUST reside in its own module under a unified demos grouping and depend on the shared
  engine library.
- **FR-003**: The pixel shooter experience MUST live in a dedicated games grouping module that consumes the shared
  engine library.
- **FR-004**: Build tooling MUST support compiling and running each demo or game module independently while reusing the
  shared engine artifacts.
- **FR-005**: Internal module dependencies MUST be expressed through local project references so no demo or game
  requires remote repositories to build.
- **FR-006**: Documentation or configuration MUST guide contributors on where to place future demos or games to preserve
  the modular structure.

### Key Entities *(include if feature involves data)*

- **Engine Library Module**: The shared collection of engine capabilities packaged once and consumed by other modules.
- **Demo Modules**: Individual showcase experiences demonstrating engine capabilities, each depending on the engine
  library while remaining isolated from one another.
- **Pixel Shooter Module**: A full game experience separated from demos but reliant on the engine library and following
  the same dependency conventions.

## Assumptions

- Current demos and the pixel shooter game already compile successfully in the monolithic setup, providing a baseline to
  validate the refactor.
- Tooling for running demos (Gradle tasks, scripts) can be updated to reflect new module paths without introducing new
  platforms or distribution targets.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Contributors can build the engine library by itself in under 5 minutes on a standard development machine.
- **SC-002**: Each demo build runs end-to-end using only declared local modules, with successful execution verified for
  100% of existing demos.
- **SC-003**: The pixel shooter build completes and launches in the refactored structure within the same runtime
  envelope as before the refactor (±5% deviation).
- **SC-004**: Onboarding documentation reflects the new modular layout, and at least one new contributor confirms they
  can locate where to add a demo without extra guidance.
