# Feature Specification: Balls Demo Slider Enhancements

**Feature Branch**: `004-add-ball-sliders`  
**Created**: 2025-10-19  
**Status**: Draft  
**Input**: User description: "Enchanse demos:balls with two more sliders. One slider will change resolution of the
simulation by changing the size of the body: the body is a square that consists of balls, the size in the number of
balls in width and height. Another slider must change the cannon ball mass. So it can affect the body in the middle with
more kinetic energy, allowing it to break tougher bodies. Add colors and labels to the sliders."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Resize the target body (Priority: P1)

As a curious player exploring the Balls demo, I want to adjust a structure size slider before firing so I can see how
different grid sizes react to impacts.

**Why this priority**: Without the ability to change the body resolution the new control scheme delivers no new value;
it underpins the feature's goal.

**Independent Test**: QA can set the slider to each extreme and confirm the rendered body updates in isolation without
requiring any other new control.

**Acceptance Scenarios**:

1. **Given** the Balls demo is loaded and no projectile is in flight, **When** the player drags the structure size
   slider to its minimum stop, **Then** the rendered square of balls shrinks to the defined minimum dimension and
   remains centered.
2. **Given** the structure size slider is at minimum, **When** the player drags it to the maximum stop and resets the
   simulation, **Then** the regenerated square reflects the maximum dimension and performance remains stable.

---

### User Story 2 - Tune projectile impact (Priority: P2)

As a player, I want to increase or decrease the cannonball mass so I can test how heavier shots break tougher bodies.

**Why this priority**: Mass control is the second pillar of the requested feature and directly impacts the ability to
break the structure.

**Independent Test**: Fire successive shots at the same structure while changing only the mass slider and observe
distinct impact outcomes.

**Acceptance Scenarios**:

1. **Given** the cannonball mass slider is at its default setting, **When** the player increases it to the highest stop
   and fires, **Then** the resulting impact shows greater displacement or breakage than the default shot.
2. **Given** the cannonball mass slider has been increased, **When** the player reduces it to the lowest stop and fires,
   **Then** the projectile causes noticeably less damage and remains consistent with the lighter-mass expectation.

---

### User Story 3 - Understand the controls instantly (Priority: P3)

As a first-time viewer, I want sliders with clear labels and distinct colors so I can quickly recognize what each
control adjusts.

**Why this priority**: Comprehension ensures the new sliders are actually used and lowers onboarding friction.

**Independent Test**: Show the paused demo to usability participants and confirm they can verbalize each slider's
purpose within seconds without prompts.

**Acceptance Scenarios**:

1. **Given** the demo is displayed at rest, **When** a viewer scans the control panel for three seconds, **Then** the
   labels convey which slider changes structure size versus cannonball mass.
2. **Given** the viewer has color vision deficiencies, **When** accessibility testing is performed, **Then** slider
   colors still provide adequate contrast and grouping is reinforced by text labels.

### Edge Cases

- What happens when the structure size slider is set to its minimum value and a projectile has already destroyed the
  body? The next reset must still rebuild a visible square that matches the minimum size.
- How does the system handle the maximum structure size in terms of performance? Slider adjustments at the top end must
  keep frame rates comparable to the current demo baseline.
- What happens if the player adjusts either slider while a projectile is mid-flight? Changes apply to the next rebuild
  or shot without glitching the active animation.
- How does the UI communicate slider defaults after players experiment? Provide a clear default marker or reset
  affordance.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Balls demo must present a labeled `Structure Size` slider, positioned with existing controls, that
  lets players choose among at least three distinct square sizes while retaining the current default as the middle
  setting.
- **FR-002**: Updating the `Structure Size` slider must regenerate the target body to the selected number of balls per
  side immediately when idle, or before the next reset if a projectile is active.
- **FR-003**: The demo must present a labeled `Cannonball Mass` slider that offers perceptible presets from 50% through
  1000% of the current default mass, keeping finer-grained control near the default while exposing heavyweight extremes.
- **FR-004**: Shots fired after a mass change must apply the new mass value to kinetic calculations so that heavier
  settings consistently produce more damage than lighter ones against the same structure.
- **FR-005**: Each slider must display its current numeric value or descriptive state alongside the handle so testers
  can confirm the selected setting.
- **FR-006**: Each slider must use a distinct, high-contrast color treatment paired with its label, staying legible for
  color-blind viewers (contrast ratio ≥ 4.5:1 against the background).

### Key Entities *(include if feature involves data)*

- **Structure Configuration**: Represents the square body's current resolution, including the selected number of balls
  per side, default value, and allowable range.
- **Projectile Settings**: Represents properties applied to each shot, including the current cannonball mass multiplier
  and the timestamp when the mass change becomes active.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In usability observation, at least 90% of first-time viewers can correctly describe both sliders' purposes
  within 10 seconds of seeing the control panel.
- **SC-002**: Adjusting the structure size slider while idle regenerates the body to the new size within 1 second, as
  measured on macOS and Windows reference machines.
- **SC-003**: QA test runs show that increasing cannonball mass to the highest setting results in at least 25% greater
  structural damage (measured by destroyed blocks) than the default mass across three consecutive shots.
- **SC-004**: Accessibility review confirms slider label text and color treatments meet WCAG AA contrast guidelines,
  documented via screenshot evidence.

### Assumptions

- Default slider values match the current Balls demo behavior (existing structure size and cannonball mass).
- Slider adjustments made mid-flight queue the change for the next rebuild or shot to avoid disrupting active
  animations.
- The control panel layout has space to host two additional sliders without redesigning the entire UI; minor spacing
  tweaks are acceptable.
