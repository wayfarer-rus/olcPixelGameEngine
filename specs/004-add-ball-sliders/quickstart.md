# Quickstart: Balls Demo Slider Enhancements

## Prerequisites

- Kotlin/Native toolchain via `./gradlew` (run outside sandbox as per repo guidance).
- GLFW installed locally (`brew install glfw` on macOS, `apt install glfw` on Ubuntu).
- Access to demo assets under `demos/shared-assets`.

## Implementation Steps

1. Update `demos/balls/src/nativeMain/kotlin/demos/balls/SliderConfig.kt` to expose slider presets and value badges.
2. Bind the `Structure Size` and `Cannonball Mass` sliders in
   `demos/balls/src/nativeMain/kotlin/demos/balls/BallsDemo.kt`, keeping the control panel responsive at 60 fps while
   covering the full 50%–1000% mass span.
3. Queue slider updates during active shots and rebuild the target body once projectiles and fragments clear.
4. Apply palette colors from `demos/balls/src/nativeMain/kotlin/demos/balls/ControlPalette.kt` and surface default
   markers for both sliders.
5. Recompute projectile momentum using the current mass multiplier before spawning new shots.

## Verification

1. From the host environment, run `./gradlew :demos:balls:assemble`.
2. Launch the demo with `./gradlew :demos:balls:runBallsReleaseExecutable<HostTarget>` and adjust the `Structure Size`
   slider between `8×8`, `12×12`, and `16×16`, confirming the square regenerates automatically and remains centered.
3. Fire three shots at `1.0×` mass, then switch to `2.0×` and capture the ≥25% damage delta (screenshots or short
   clips).
4. Return the mass slider to `1.0×`, confirm the grey default marker aligns with the handle, and verify the value badges
   above each slider reflect the current setting.
5. Take a screenshot that includes the entire control panel, ensuring palette colors and labels meet the documented
   contrast ratios.
6. Document results in `docs/demos/balls/slider-enhancements.md`, attaching contrast meter readouts and performance
   observations.

## Troubleshooting

- If slider updates do not apply during projectile flight, ensure deferred updates trigger during the cleanup phase.
- On color issues, verify panel background color and adjust hex values until contrast ≥ 4.5:1 against text.
- For build failures, confirm local Kotlin version matches `kotlin_version` in `gradle.properties` (1.3.71).
