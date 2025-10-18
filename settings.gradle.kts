rootProject.name = "olcGameEnginePort"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":engine",
    ":demos:fireworks",
    ":demos:asteroids",
    ":demos:breakout",
    ":demos:boids",
    ":demos:destructible_sprite",
    ":demos:balls",
    ":demos:mandelbrot",
    ":demos:bejewelled",
    ":demos:sample_app",
    ":demos:dungeon_warping",
    ":demos:shared-assets",
    ":games:pixel_shooter"
)
