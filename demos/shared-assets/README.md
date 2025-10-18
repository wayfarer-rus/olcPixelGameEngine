# Shared Demo Assets

Store reusable textures or sprites for demo modules in `src/nativeMain/resources`. Demo projects can consume these
assets by declaring `implementation(project(":demos:shared-assets"))` in their Gradle dependencies and loading the
resource path relative to this module.
