plugins {
    id("olc.game_engine.demo")
}

demoModule {
    applicationName = "BallsDemo"
    entryPoint = "demos.balls.main"
}

kotlin {
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(project(":demos:slider"))
            }
        }
    }
}
