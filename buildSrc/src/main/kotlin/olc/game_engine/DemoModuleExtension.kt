package olc.game_engine

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class DemoModuleExtension @Inject constructor(objects: ObjectFactory) {
    private val applicationNameProperty: Property<String> =
        objects.property(String::class.java)
    private val entryPointProperty: Property<String> =
        objects.property(String::class.java)
    val dependencies: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())
    val resourceDirs: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())

    var applicationName: String
        get() = applicationNameProperty.orNull ?: ""
        set(value) {
            applicationNameProperty.set(value)
        }

    var entryPoint: String
        get() = entryPointProperty.orNull ?: ""
        set(value) {
            entryPointProperty.set(value)
        }

    internal fun applicationNameProperty(): Property<String> = applicationNameProperty

    internal fun entryPointProperty(): Property<String> = entryPointProperty
}
