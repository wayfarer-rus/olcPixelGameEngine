# olcPixelGameEngine
kotlin-native port of olcPixelGameEngine, a tool used in javidx9's YouTube videos and projects

Tested on windows 10 and macOS Mojave

# Thanks and Inspirations
All credit goes to https://github.com/OneLoneCoder

Huge thanks to the awesome dude who created this: https://github.com/Dominaezzz/kgl

# How to build?
You have to have gradle tool installed.

You have to have GLFW installed. Call `brew install glfw` on Mac; `apt install glfw` on Ubuntu

Windows (at least in my experience) already have GLFW

Then do `gradle assemble` or `gradle runOlcGameEnginePortSampleAppReleaseExecutableOlcGameEnginePort`

Assemble will create executable in `build/bin` catalog. And second command will run sample app.

Couple of demo apps also available and can be directly compiled and run from cmd:
- Fireworks demo:
    `gradle runFireworksDemoReleaseExecutableOlcGameEnginePort`
- Asteroids demo:
    `gradle runAsteroidsDemoReleaseExecutableOlcGameEnginePort`  

# Disclaimer
~~Performance is shit =)~~ 

~~I blame garbage collector for it. Probably will do profiling and optimization.~~

I've made some optimizations with some help from Kotlin native team. 
Now sample app runs with stable 80 fps after short worm-up.

Will do an additional step after kgl updates to 1.3.50 version of kotlin-multiplatform.

Port is not fully complete:
- PNG sprite loading not yet ported
- Texture pack support are not ported
- Here and there you may find a missing function
- All unnecessary UInts replaced with Ints

But the core functionality is there.
