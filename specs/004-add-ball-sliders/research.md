# Research Summary: Balls Demo Slider Enhancements

## Slider Range Decisions

- **Decision**: Use structure size options of 8×8, 12×12, and 16×16 balls with the current demo default of 12×12.
- **Rationale**: Keeps minimum dense enough to stay readable, maximum within performance headroom measured in existing
  demo runs, and preserves default as middle option per spec.
- **Alternatives considered**: Smaller grids (4×4, 6×6) looked too sparse for the physics showcase; larger grids (
  20×20+) risk frame drops on low-end hardware.

## Cannonball Mass Scaling

- **Decision**: Provide seven mass multipliers: 0.5×, 0.75×, 1.0×, 1.25×, 1.5×, 1.75×, 2.0×.
- **Rationale**: Meets “at least five evenly spaced settings” by stepping every 0.25×, keeps default at 1.0×, and offers
  noticeable differences for QA impact comparisons.
- **Alternatives considered**: Non-linear scaling (e.g., exponential) felt harder for players to predict; fewer steps
  reduced fidelity around the default mass.

## Slider Accessibility Treatments

- **Decision**: Pair each slider with color tokens meeting WCAG AA (e.g., deep blue for structure size, amber for mass)
  plus label text and numeric value badges.
- **Rationale**: Color + text supports color-blind users, and contrast-friendly palettes maintain legibility against the
  existing dark UI.
- **Alternatives considered**: Relying solely on colored tracks failed accessibility audits; using identical slider
  styling risked confusion when quickly scanning controls.
