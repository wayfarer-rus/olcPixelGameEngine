# Balls Demo Slider Enhancements

## Overview

This document tracks the configuration ranges, QA checkpoints, and accessibility evidence for the Structure Size and
Cannonball Mass sliders introduced in the Balls demo.

### Slider Ranges

- **Structure Size**: `8×8`, `12×12` (default), `16×16`
- **Cannonball Mass**: `0.5×`, `0.75×`, `1.0×` (default), `1.5×`, `2.0×`, `3.0×`, `5.0×`, `7.5×`, `10.0×`

### Open Questions

- Capture rendering performance metrics for each structure size combination.
- Record per-platform behavior (macOS, Linux, Windows) once QA runs complete.

## QA Checklist

1. Set Structure Size to minimum (`8×8`), fire once, and confirm the square rebuilds centered on reset.
2. Set Structure Size to maximum (`16×16`) and verify frame rate remains smooth during idle and heavy impacts.
3. Fire three shots at default mass, then repeat at `2.0×` mass and confirm >25% additional breakage.
4. Fire at `0.5×` mass to ensure the structure survives lighter impacts as expected.
5. Toggle sliders mid-flight to confirm updates defer until the next reset/shot without glitches.

### Structure Size Verification Notes

- Record screenshots of the playfield at `8×8`, `12×12`, and `16×16` settings with the value badge visible above the
  slider.
- While targets are at `8×8`, ensure the cannon muzzle alignment still points to the body center (offset updated).
- After sliding from `16×16` back to `12×12`, confirm the body regenerates automatically once all projectiles clear.

### Mass Impact Verification Notes

- Capture side-by-side footage comparing `0.5×`, `1.0×`, `2.0×`, and `10.0×` shots against the `12×12` structure,
  highlighting fragment counts and breakage.
- Log observed debris counts or remaining blocks after each mass setting to quantify the ≥25% damage delta; heavier
  settings now multiply energy transfer by the mass factor, carve an expanding blast radius, and emit debris equal to
  displaced circles.
- Confirm mass slider updates take effect on the next fired projectile even if fragments from the previous shot remain
  onscreen.

## Accessibility Evidence

- **Color Tokens**:
    - Structure Size text/track/handle use `#BADBFF`, `#3A69BF`, `#78B7FF` (contrast ≥ 6.5:1 against panel background
      `#181818`).
    - Cannonball Mass text/track/handle use `#FFE0B0`, `#BE8420`, `#FFC664` (contrast ≥ 5.2:1 against panel background).
    - Toughness text/track/handle use `#D2D2D2`, `#828282`, `#CDCDCD` (contrast ≥ 7.0:1 against panel background).
- **Contrast Checks**: Capture screenshots with contrast measurements ≥ 4.5:1 and store meter readouts in this folder.
- **Labels & Badges**: Verify label typography and badge text remain legible at 100% and 125% UI scale.
- **Default Markers**: Confirm grey tick marks remain visible in both light and dark screenshot exports.

## Artifact Log

- `TODO`: Attach final screenshots highlighting slider colors and contrast ratios.
- `TODO`: Add GIF of sliders demonstrating immediate structure rebuild and mass-based destruction.

## QA Outcomes Tracker

- [ ] Structure slider defaults to `12×12` with grey marker centered (screenshot link).
- [ ] Mass slider default marker aligns with `1.0×` and badge reads `100%`.
- [ ] `/specs/004-add-ball-sliders/contracts/balls-demo-controls.yaml` exercised via GET/POST to confirm settings
  exchange.
- [ ] Contrast meter readouts stored alongside screenshots in `docs/demos/balls/`.
