package demos.balls

import kotlin.math.roundToInt

/**
 * Represents the selectable dimensions of the square body shown in the Balls demo.
 * Tracks the current value along with a queued value to apply once the simulation is idle.
 */
class StructureConfiguration(
    val availableSizes: List<Int> = listOf(8, 12, 16),
    val defaultSize: Int = 12
) {

    init {
        require(availableSizes.isNotEmpty()) { "availableSizes cannot be empty" }
        require(defaultSize in availableSizes) { "defaultSize must be one of the available sizes" }
    }

    var currentSize: Int = defaultSize
        private set

    private var pendingSize: Int? = null

    fun sliderPosition(): Float = sliderPositionFor(currentSize)

    fun sliderPositionFor(size: Int): Float {
        val index = availableSizes.indexOf(size).takeIf { it >= 0 } ?: 0
        val segments = (availableSizes.size - 1).coerceAtLeast(1)
        return index.toFloat() / segments
    }

    fun queueFromSlider(position: Float) {
        pendingSize = sizeFor(position)
    }

    fun hasPendingChange(): Boolean = pendingSize != null && pendingSize != currentSize

    fun applyPendingIfPresent(): Boolean {
        val next = pendingSize ?: return false
        if (next == currentSize) {
            pendingSize = null
            return false
        }
        currentSize = next
        pendingSize = null
        return true
    }

    fun reset() {
        currentSize = defaultSize
        pendingSize = null
    }

    fun sizeFor(position: Float): Int {
        if (availableSizes.size == 1) return availableSizes.first()
        val clamped = position.coerceIn(0f, 1f)
        val scaledIndex = (clamped * (availableSizes.size - 1)).roundToInt()
        return availableSizes[scaledIndex]
    }
}

/**
 * Encapsulates cannonball mass configuration and pending slider updates.
 */
class ProjectileSettings(
    val massMultipliers: List<Float> = listOf(
        0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 3.0f, 5.0f, 7.5f, 10.0f
    ),
    val defaultMultiplier: Float = 1.0f
) {

    init {
        require(massMultipliers.isNotEmpty()) { "massMultipliers cannot be empty" }
        require(defaultMultiplier in massMultipliers) { "defaultMultiplier must be one of the mass multipliers" }
    }

    var currentMultiplier: Float = defaultMultiplier
        private set

    private var pendingMultiplier: Float? = null

    fun sliderPosition(): Float = sliderPositionFor(currentMultiplier)

    fun sliderPositionFor(multiplier: Float): Float {
        val index = massMultipliers.indexOf(multiplier).takeIf { it >= 0 } ?: 0
        val segments = (massMultipliers.size - 1).coerceAtLeast(1)
        return index.toFloat() / segments
    }

    fun queueFromSlider(position: Float) {
        pendingMultiplier = multiplierFor(position)
    }

    fun hasPendingChange(): Boolean = pendingMultiplier != null && pendingMultiplier != currentMultiplier

    fun applyPendingIfPresent(): Boolean {
        val next = pendingMultiplier ?: return false
        if (next == currentMultiplier) {
            pendingMultiplier = null
            return false
        }
        currentMultiplier = next
        pendingMultiplier = null
        return true
    }

    fun reset() {
        currentMultiplier = defaultMultiplier
        pendingMultiplier = null
    }

    fun multiplierFor(position: Float): Float {
        if (massMultipliers.size == 1) return massMultipliers.first()
        val clamped = position.coerceIn(0f, 1f)
        val scaledIndex = (clamped * (massMultipliers.size - 1)).roundToInt()
        return massMultipliers[scaledIndex]
    }

    fun badgeFor(multiplier: Float = currentMultiplier): String =
        "${(multiplier * 100f).roundToInt()}%"
}

/**
 * Holds presentation-specific metadata for sliders, including accessible color token names
 * and the text shown alongside the current value.
 */
data class ControlPresentation(
    val theme: ControlPalette.SliderTheme,
    var valueBadge: String
) {
    companion object {
        fun structureBadge(size: Int): String = "${size}x${size}"
    }
}
