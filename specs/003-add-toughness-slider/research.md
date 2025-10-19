# Research Summary

### Reuse Existing Slider Module

- **Decision**: Reuse the slider UI and input handling patterns from `:demos:slider` for the Balls demo toughness
  control.
- **Rationale**: Guarantees visual and behavioral consistency across demos while avoiding duplicate widget logic and
  styling work.
- **Alternatives considered**: Building a bespoke slider for the Balls demo (discarded due to higher effort and risk of
  inconsistent UX); mapping toughness to keyboard shortcuts (rejected because it hides the control from viewers).

### Toughness to Simulation Mapping

- **Decision**: Map the slider’s 0–100 value to a normalized stiffness or resistance coefficient that scales the central
  body’s response during every physics update tick.
- **Rationale**: Keeps the control intuitive while enabling continuous tuning without restarting the simulation loop.
- **Alternatives considered**: Applying discrete presets (low/medium/high) which would reduce granularity; delaying
  updates until slider release, which would hinder real-time feedback.
