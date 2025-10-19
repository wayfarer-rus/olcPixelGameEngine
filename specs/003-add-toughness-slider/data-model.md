# Data Model

## Toughness Control Session State

- **Purpose**: Represents the viewer-selected toughness level for the Balls demo during a single run session.
- **Fields**:
    - `value` (Int, 0–100, default 0) – raw slider position.
    - `normalizedResistance` (Float, 0.0–1.0, derived) – value mapped to physics coefficient.
- **Relationships**: Feeds into Central Body Simulation Parameters on every update tick.
- **Validation Rules**:
    - Reject values outside the inclusive range 0–100.
    - Recalculate `normalizedResistance` immediately after any accepted change.
- **State Transitions**:
    - `INITIAL` (value = 0) → `ADJUSTED` when the viewer moves the slider at least once.
    - Remains `ADJUSTED` for subsequent changes; resets to `INITIAL` only when the demo session restarts.

## Central Body Simulation Parameters

- **Purpose**: Encapsulates the elasticity and energy transfer coefficients that determine how the central body responds
  to collisions.
- **Fields**:
    - `baseStiffness` (Float) – current baseline taken from existing demo configuration.
    - `resistanceMultiplier` (Float) – multiplier derived from Toughness Control Session State.
- **Relationships**: Consumed by the physics integrator that governs inter-ball forces.
- **Validation Rules**:
    - `resistanceMultiplier` must stay within a safe numeric range (e.g., clamp 0.0–4.0) to avoid destabilising the
      simulation.
- **State Transitions**:
    - Updated each frame using latest `normalizedResistance`.
    - Reverts to baseline parameters when session resets or slider returns to 0.
