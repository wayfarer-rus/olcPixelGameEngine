# Phase 0 Research

## Decision: Package plugin in `buildSrc` using Kotlin DSL `Plugin<Project>` implementation

- **Rationale**: Keeps configuration local to repository, automatically on Gradle classpath, and avoids distributing a
  separate artifact while enabling typed DSL for consumers. Aligns with requirement to "implement buildSrc plugin (
  local)".
- **Alternatives considered**:
    - *Standalone included build*: Adds overhead for maintenance and publication; unnecessary for repo-scoped plugin.
    - *Convention script via `gradle` folder*: Less flexible for validation logic and harder to unit test.

## Decision: Reuse existing host OS detection with configurable binary name and entry point

- **Rationale**: Current demos derive host target via Kotlin/Native presets; encapsulating this logic in the plugin
  ensures parity and minimises migration risk.
- **Alternatives considered**:
    - *Force all targets regardless of host*: Increases build time and fails on unsupported hosts.
    - *Require maintainers to pass target manually*: Reintroduces configuration complexity the plugin aims to hide.

## Decision: Provide Gradle TestKit-based verification suite

- **Rationale**: Validates plugin behaviour (default dependencies, validation errors) in realistic build scenarios
  without manual demos. Matches testing guideline focus on scenario-driven tests and keeps feedback fast.
- **Alternatives considered**:
    - *Rely on manual demo builds only*: Prone to regressions and hard to automate.
    - *Unit tests with mocked Project*: Less confidence for Gradle DSL interactions and binary generation.

## Decision: Extension DSL exposes required fields plus hooks for optional configuration

- **Rationale**: Meets requirement to "specifying only application name and the path to the main" while leaving room for
  advanced demo needs (additional dependencies/resources) through structured extension.
- **Alternatives considered**:
    - *Hard-code dependencies without extension hooks*: Limits future customisation, conflicts with FR-005.
    - *Allow arbitrary project configuration via lambda*: Risks inconsistent builds and defeats standardisation goal.

## Decision: Validation fails during configuration with actionable messages

- **Rationale**: Aligns with User Story 3 and SC-003; early failure reduces wasted build time and clarifies
  misconfigurations.
- **Alternatives considered**:
    - *Defer validation to execution*: Produces confusing runtime errors later in the pipeline.
    - *Log warnings without halting*: Allows broken demos to slip through and violates guardrail requirement.
