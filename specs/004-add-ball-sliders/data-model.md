# Data Model: Balls Demo Slider Enhancements

## Entities

### StructureConfiguration

- **Description**: Captures the square body resolution exposed to players.
- **Fields**:
    - `availableSizes`: ordered list `[8, 12, 16]` (balls per side).
    - `defaultSize`: `12` (balls per side).
    - `currentSize`: one of `availableSizes`, mutable via slider.
- **Validation Rules**:
    - `currentSize` must always equal an entry in `availableSizes`.
    - Regeneration routine must rebuild an `currentSize × currentSize` grid centered in the playfield.
- **State Transitions**:
    - `Idle` → `PendingRebuild` when slider changes while projectile mid-flight.
    - `PendingRebuild` → `Idle` after grid is reconstructed post-impact/reset.

### ProjectileSettings

- **Description**: Parameterizes cannonball physics for the demo.
- **Fields**:
    - `massMultipliers`: ordered list `[0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0]`.
    - `defaultMultiplier`: `1.0`.
    - `currentMultiplier`: value from `massMultipliers`.
- **Validation Rules**:
    - `currentMultiplier` cannot change during an active projectile but must apply to the next shot.
    - Physics step must recompute momentum using `baseMass * currentMultiplier`.
- **State Transitions**:
    - `Ready` → `Firing` when launch triggered.
    - `Firing` → `CoolingDown` on collision resolution.
    - `CoolingDown` → `Ready` once post-impact cleanup completes and deferred multiplier updates take effect.

### ControlPresentation

- **Description**: Holds UI-facing information for slider rendering.
- **Fields**:
    - `sliderLabel`: localized string (`"Structure Size"`, `"Cannonball Mass"`).
    - `colorToken`: accessible color names (`"deepBlue"`, `"amber"`).
    - `valueBadge`: formatted text (e.g., `"12×12"`, `"125%"`).
- **Validation Rules**:
    - Colors must meet WCAG AA contrast against panel background.
    - Value badges must refresh whenever underlying configuration changes.

## Relationships

- `StructureConfiguration` and `ProjectileSettings` both expose `ControlPresentation` data for the UI layer.
- Game loop reads `currentSize` to rebuild bodies and `currentMultiplier` to compute shot momentum; no persistent
  storage required.
