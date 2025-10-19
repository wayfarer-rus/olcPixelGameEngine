# Feature Specification: Balls Demo Toughness Slider

**Feature Branch**: `003-add-toughness-slider`  
**Created**: 2025-10-19  
**Status**: Draft  
**Input**: User description: "enhance balls demo. Take :demos:balls and add there a slider (you can take one from :
demos:slider) that will modify the toughness of the body that we simulate in the middle of the screen. Default toughness
is 0, max toughness is 100. Toughness affect physics of the balls in the body: how many energy they need to be moved
from"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Adjust Toughness In Real Time (Priority: P1)

As a viewer of the Balls demo, I adjust a toughness slider to immediately feel how the central body resists the orbiting
balls.

**Why this priority**: Real-time control over the demo’s headline mechanic delivers the primary learning value of the
update.

**Independent Test**: Launch the Balls demo, move the toughness slider through several positions, and verify the central
body’s resistance changes without restarting the simulation.

**Acceptance Scenarios**:

1. **Given** the Balls demo is running with the toughness slider in view, **When** the user drags the slider toward
   higher values, **Then** the central body noticeably resists deformation and ball displacement more strongly.
2. **Given** the slider is adjusted toward lower values, **When** the user observes the simulation, **Then** the central
   body behaves like the current baseline with minimal additional resistance.

---

### User Story 2 - Understand Toughness Settings (Priority: P2)

As a demo viewer, I can read the slider label and numeric value so I know the current toughness level and how far I have
changed it from the default.

**Why this priority**: Transparent feedback helps viewers connect the control to the observed physics and encourages
experimentation.

**Independent Test**: Load the demo, note the default slider label and value, change the slider, and confirm the
displayed value updates to match the new setting.

**Acceptance Scenarios**:

1. **Given** the demo has just loaded, **When** the viewer checks the toughness control, **Then** it is labeled clearly
   and shows the default value of 0.
2. **Given** the viewer releases the slider at any position between 0 and 100, **When** they glance at the control, *
   *Then** the displayed numeric value matches the chosen toughness level.

---

### Edge Cases

- Slider at minimum (0) keeps the simulation identical to today’s baseline with no unexpected resistance spikes.
- Slider at maximum (100) makes the body’s resistance obvious without freezing or destabilising the simulation.
- Rapid slider sweeps from 0 to 100 and back do not cause the demo to lag, flicker, or lose track of the current value.
- Leaving the slider untouched while the demo loops continues to display the default value and behaviour reliably.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Balls demo MUST present a prominently labeled “Toughness” slider control within the main demo
  viewport.
- **FR-002**: The toughness slider MUST initialise at value 0 on every launch so the demo opens with today’s baseline
  behaviour.
- **FR-003**: The slider MUST support all integer values from 0 through 100 inclusive, with 0 as minimum and 100 as
  maximum.
- **FR-004**: Adjusting the slider MUST change the central body’s resistance in real time: higher values increase the
  energy required to displace the body, lower values keep it loose.
- **FR-005**: The demo MUST display the current toughness value numerically beside or on the slider so users can read
  their setting at a glance.
- **FR-006**: The demo MUST preserve overall performance while the slider is moved repeatedly, with no noticeable frame
  drops or simulation resets triggered solely by slider use.

### Key Entities *(include if feature involves data)*

- **Toughness Level**: The user-controlled value (0–100) describing how resistant the central body should be; defaults
  to 0 and is persisted only for the current session.
- **Central Body Simulation**: The cluster of balls or particles in the centre of the demo that deforms when struck; its
  responsiveness is modulated by the toughness level.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In moderated testing, 90% of viewers adjust the toughness slider within 30 seconds of launching the Balls
  demo without guidance.
- **SC-002**: Observers report a clearly noticeable change in the central body’s resistance at both the minimum and
  maximum slider positions in 95% of trial runs.
- **SC-003**: During a five-minute demo session, no more than one frame hitch longer than 0.1 seconds is attributable to
  manipulating the toughness slider.
- **SC-004**: Demo feedback indicates at least 80% of respondents understand the purpose of the toughness control after
  a single interaction.

## Assumptions

- The existing slider presentation from other demos can be reused to maintain look-and-feel consistency.
- Toughness at value 0 matches the current public Balls demo behaviour; no other baseline tuning is required.
- Toughness adjustments apply instantly without pausing or restarting the simulation.

