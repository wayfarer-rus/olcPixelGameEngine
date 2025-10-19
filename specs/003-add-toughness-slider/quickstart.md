# Quickstart: Balls Demo Toughness Slider

1. **Install native prerequisites**  
   Ensure Kotlin/Native dependencies are ready and GLFW is installed (`brew install glfw` on macOS, `apt install glfw`
   on Ubuntu, or configure `MINGW64_DIR` on Windows).

2. **Build the engine and demos**
   ```bash
   ./gradlew :engine:assemble
   ./gradlew :demos:balls:assemble
   ```

3. **Run the Balls demo with slider**
   ```bash
   ./gradlew :demos:balls:runBallsDemoReleaseExecutable<HostTarget>
   ```
   Replace `<HostTarget>` with the local Kotlin/Native target (e.g., `MacosX64`, `LinuxX64`, `MingwX64`).

4. **Exercise the feature**
    - Confirm the toughness slider appears with a default value of 0 and a matching on-screen label.
    - Drag the slider to multiple positions (e.g., 25, 50, 100) and verify the central body becomes progressively more
      resistant to incoming projectiles.
    - At toughness 100 (mapped to the previous “60” behaviour), confirm that only the heaviest hits dislodge particles;
      at 0 the body should fracture much more easily.
    - Press `R` to reset the demo and confirm the slider snaps back to 0 and the label updates accordingly.
    - Sweep the slider quickly between extremes to ensure the simulation stays smooth with no more than one >0.1s hitch.

5. **Optional regression checks**
    - Launch another demo (e.g., `:demos:slider`) to ensure shared UI assets were unaffected.
    - Run native tests if modified engine logic is involved:
      ```bash
      ./gradlew :engine:check
      ```
