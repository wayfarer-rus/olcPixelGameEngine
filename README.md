# olcPixelGameEngine
kotlin-native port of olcPixelGameEngine, a tool used in javidx9's YouTube videos and projects

Tested on windows 10 and macOS Mojave

# Thanks and Inspirations
All credit goes to https://github.com/OneLoneCoder

Huge thanks to the awesome dude who created this: https://github.com/Dominaezzz/kgl

# How to build?

Use the bundled Gradle wrapper (`./gradlew`) so the correct Kotlin/Native toolchain is selected.

Install GLFW before building. Call `brew install glfw` on macOS or `apt install glfw` on Ubuntu.

Build artifacts target the host platform (macOS, Linux, Windows). Ensure `MINGW64_DIR` points to a valid MinGW64 path
when building on Windows.

## Install GLFW on Windows
Download and install [msys2](https://www.msys2.org/)

Run `msys` and install `glfw3` for `mingw64`

You want to have `c:\msys64\mingw64\bin` in your `PATH`, but I stumbled upon an issue.
When I added it directly into Windows Environment, linking of the project started to fail.
So, I tried to add it in project configuration, like `Path=c:\msys64\mingw64\bin\\;%Path%` and it works like that. 

## Assemble and Run

Build everything:

```
./gradlew assemble
```

Build the reusable engine library only:

```
./gradlew :engine:assemble
```

Run the sample app:

```
./gradlew :demos:sample_app:runSampleAppReleaseExecutableMacosX64
```

Each demo now lives in its own module under `demos/*`. Launch them with the corresponding Gradle task:

| Demo                | Command                                                                                  |
|---------------------|------------------------------------------------------------------------------------------|
| Fireworks           | `./gradlew :demos:fireworks:runFireworksDemoReleaseExecutableMacosX64`                   |
| Asteroids           | `./gradlew :demos:asteroids:runAsteroidsDemoReleaseExecutableMacosX64`                   |
| Breakout            | `./gradlew :demos:breakout:runBreakoutDemoReleaseExecutableMacosX64`                     |
| Boids               | `./gradlew :demos:boids:runBoidsDemoReleaseExecutableMacosX64`                           |
| Destructible Blocks | `./gradlew :demos:destructible_sprite:runDestructibleBlockDemoReleaseExecutableMacosX64` |
| Balls               | `./gradlew :demos:balls:runBallsDemoReleaseExecutableMacosX64`                           |
| Mandelbrot          | `./gradlew :demos:mandelbrot:runMandelbrotDemoReleaseExecutableMacosX64`                 |
| Sort-of Bejewelled  | `./gradlew :demos:bejewelled:runBejewelledDemoReleaseExecutableMacosX64`                 |
| Dungeon Warping     | `./gradlew :demos:dungeon_warping:runDungeonWarpingDemoReleaseExecutableMacosX64`        |

The pixel shooter game now lives under `games/pixel_shooter`:

```
./gradlew :games:pixel_shooter:runPixelShooterGameReleaseExecutableMacosX64
```

All build outputs land in `build/bin/<module>/` for the selected target.

*Run task suffixes reflect the host target (e.g., `MacosX64`). Adjust the suffix for your platform when executing these
commands.*

## Project Layout

```
engine/             # Shared engine library (Kotlin/Native)
demos/              # Individual demo executables
  fireworks/
  asteroids/
  breakout/
  boids/
  destructible_sprite/
  balls/
  mandelbrot/
  bejewelled/
  sample_app/
  dungeon_warping/
  shared-assets/
games/              # Game executables (pixel shooter, etc.)
```

# Disclaimer
~~Performance is shit =)~~ 

~~I blame garbage collector for it. Probably will do profiling and optimization.~~

I've made some optimizations with some help from Kotlin native team. 
Now sample app runs with stable 80 fps after short worm-up.

~~Will do an additional step after kgl updates to 1.3.50 version of kotlin-multiplatform.~~

Port is not fully complete:
- PNG sprite loading not yet ported
- Texture pack support are not ported
- Here and there you may find a missing function
- All unnecessary UInts replaced with Ints

But the core functionality is there.
