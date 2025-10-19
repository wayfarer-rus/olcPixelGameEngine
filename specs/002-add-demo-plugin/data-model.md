# Data Model

## Entity: DemoModuleConfig

- **Description**: Declarative information supplied by demo maintainers via the plugin extension.
- **Fields**:
    - `applicationName` (String, required): Display name and executable identifier; must be unique among demos.
    - `entryPoint` (String, required): Fully qualified Kotlin function reference that resolves to the demo's `main`
      entry point.
    - `dependencies` (List<String>, optional): Additional Gradle dependency notations appended to the default
      configuration.
    - `resourceDirs` (List<String>, optional): Relative paths to extra resource directories to include alongside shared
      assets.
- **Validation Rules**:
    - `applicationName` cannot be blank and must differ from existing binaries in `demos/`.
    - `entryPoint` must match the Kotlin package naming pattern and must resolve during compilation.
    - Custom `dependencies` and `resourceDirs` are optional but must refer to existing coordinates/paths at
      configuration time.

## Entity: DemoBuildDefaults

- **Description**: Shared configuration automatically applied by the plugin.
- **Fields**:
    - `engineDependency` (ProjectReference): Points to `:engine` module.
    - `sharedAssetsDependency` (ProjectReference): Points to `:demos:shared-assets` module resources.
    - `languageSettings` (Set<String>): Standard language features and opt-ins required by demos.
    - `targets` (Set<String>): Host OS-specific Kotlin/Native presets (`macosX64`, `linuxX64`, `mingwX64`).
- **Validation Rules**:
    - Targets list must include only presets supported by the local host.
    - Language settings must be applied uniformly across `nativeMain` and `nativeTest` source sets.

## Relationships & State

- `DemoModuleConfig` is supplied per demo module and merged with `DemoBuildDefaults` during Gradle configuration.
- Validation occurs before task execution; failure halts configuration to provide actionable feedback.
- Successful validation results in generated binaries named after `applicationName` and configured to run `entryPoint`.
