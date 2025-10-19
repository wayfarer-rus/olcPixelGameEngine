package demos.balls

import olc.game_engine.Pixel

/**
 * Centralizes slider labels and color tokens with contrast-friendly values.
 * Colors are selected against the Balls demo's dark background to meet WCAG AA (≥4.5:1).
 */
object ControlPalette {

    data class ColorToken(val name: String, val pixel: Pixel)

    data class SliderTheme(
        val label: String,
        val textColor: ColorToken,
        val trackColor: ColorToken,
        val handleColor: ColorToken,
        val markerColor: ColorToken
    )

    val structure: SliderTheme = SliderTheme(
        label = "Structure Size",
        textColor = ColorToken("structure-text", Pixel(186, 219, 255)),
        trackColor = ColorToken("structure-track", Pixel(58, 105, 191)),
        handleColor = ColorToken("structure-handle", Pixel(120, 183, 255)),
        markerColor = ColorToken("structure-marker", Pixel(90, 125, 196))
    )

    val mass: SliderTheme = SliderTheme(
        label = "Cannonball Mass",
        textColor = ColorToken("mass-text", Pixel(255, 224, 176)),
        trackColor = ColorToken("mass-track", Pixel(190, 132, 32)),
        handleColor = ColorToken("mass-handle", Pixel(255, 198, 100)),
        markerColor = ColorToken("mass-marker", Pixel(200, 150, 70))
    )

    val toughness: SliderTheme = SliderTheme(
        label = "Toughness",
        textColor = ColorToken("toughness-text", Pixel(210, 210, 210)),
        trackColor = ColorToken("toughness-track", Pixel(130, 130, 130)),
        handleColor = ColorToken("toughness-handle", Pixel(205, 205, 205)),
        markerColor = ColorToken("toughness-marker", Pixel(150, 150, 150))
    )
}
