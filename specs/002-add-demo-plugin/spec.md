# Feature Specification: BuildSrc Demo Module Plugin

**Feature Branch**: `002-add-demo-plugin`  
**Created**: 2025-10-19  
**Status**: Draft  
**Input**: User description: "implement buildSrc plugin (local) that will allow easily define demo projects, specifying
only application name and the path to the main, hiding all complexity of multiplatform kotlin project definitions."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - One-Step Demo Setup (Priority: P1)

Repository maintainers can stand up a new demo module by declaring the plugin with the application's display name and
the entry point path, without hand-writing multiplatform build logic.

**Why this priority**: New demos are currently slow and error-prone to register because teams copy large build files;
reducing that friction unlocks faster experimentation.

**Independent Test**: Configure a fresh demo module using only the plugin inputs, run the standard demo build task, and
confirm a runnable executable is produced with the supplied name and entry point.

**Acceptance Scenarios**:

1. **Given** a new demo module that references the plugin with application name "OrbitDemo" and entry point "
   demos.orbit.main", **When** a maintainer runs the usual demo build command, **Then** the build creates a runnable
   executable for the maintainer's operating system without additional configuration changes.
2. **Given** a demo module configured only with the required plugin inputs, **When** the project metadata is inspected,
   **Then** the module shows the shared engine dependency and default language settings applied automatically.

---

### User Story 2 - Consistent Demo Maintenance (Priority: P2)

Maintainers can migrate or update existing demo modules to the plugin so that all demos share the same defaults and
future tweaks happen in one place.

**Why this priority**: Centralising the configuration prevents drift between demos and lowers the overhead of future
engine upgrades.

**Independent Test**: Replace the manual build file of an existing demo with the plugin configuration, run the existing
demo's build command, and compare outputs to confirm no behavioural regressions.

**Acceptance Scenarios**:

1. **Given** an existing demo that is switched to the plugin-based configuration, **When** the demo is built and
   launched, **Then** its executable name, entry point, and runtime behaviour match the pre-migration baseline.

---

### User Story 3 - Guardrail Feedback (Priority: P3)

Maintainers receive immediate, actionable feedback when the required plugin inputs are missing or incorrect so they can
correct issues before a build attempt fails deeply in the toolchain.

**Why this priority**: Early validation saves time by eliminating hard-to-debug build errors caused by typos or
misconfigured entry points.

**Independent Test**: Provide invalid or blank plugin inputs in a demo module, trigger the configuration phase, and
confirm the plugin stops the process with a clear message that explains how to fix the issue.

**Acceptance Scenarios**:

1. **Given** a demo module with an entry point that does not resolve to a callable function, **When** the project is
   evaluated, **Then** the build fails fast with a human-readable message describing the missing entry point.
2. **Given** a demo module that omits the application name, **When** the project is evaluated, **Then** the build stops
   immediately and tells the maintainer to provide a unique application name.

---

### Edge Cases

- What happens when two demo modules request the same application name or binary identifier?
- How does the system handle a plugin declaration placed outside the `demos/` directory structure?
- What happens when the host operating system is not one of the supported desktop targets?
- How does the system behave if additional dependencies or resources are declared alongside the plugin defaults?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The plugin MUST allow maintainers to define a demo module by providing only an application display name
  and a fully qualified entry point path.
- **FR-002**: The plugin MUST automatically configure the demo module to target the supported desktop operating systems
  without requiring manual duplication of build logic.
- **FR-003**: The plugin MUST attach the shared game engine and shared demo assets as default dependencies for every
  demo module that applies it.
- **FR-004**: The plugin MUST apply the standard language defaults and experimental feature opt-ins required for demos
  to compile consistently across platforms.
- **FR-005**: The plugin MUST expose an extension point so maintainers can declare additional dependencies or resources
  while retaining the base configuration.
- **FR-006**: The plugin MUST validate the supplied inputs and fail fast with clear guidance when values are missing,
  duplicated, or reference an entry point that cannot be resolved.

### Key Entities *(include if feature involves data)*

- **Demo Module Definition**: Describes the metadata required to register a demo (application name, entry point path,
  optional additional dependencies).
- **Demo Build Defaults**: Captures the shared configuration applied to every demo (target platforms, language settings,
  shared dependencies).

## Assumptions

- Maintainers creating demos are comfortable editing module-level build files and following documented naming
  conventions.
- All demos rely on the shared engine module and shared demo assets for runtime behaviour.
- Desktop operating systems remain the only supported targets for demos within this release.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Setting up a brand-new demo module takes less than 10 minutes using the plugin, compared to the current
  30-minute baseline.
- **SC-002**: At least three existing demos are migrated to the plugin configuration and pass their standard smoke tests
  without behavioural regressions.
- **SC-003**: 100% of missing or incorrect plugin inputs are caught during project evaluation with a descriptive
  message, eliminating deep build-time failures caused by misconfiguration.
- **SC-004**: In the post-rollout maintainer survey, the perceived clarity of demo setup documentation scores an average
  of 4 out of 5 or higher.
