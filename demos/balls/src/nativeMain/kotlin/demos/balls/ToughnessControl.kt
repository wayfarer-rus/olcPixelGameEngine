package demos.balls

import kotlin.math.roundToInt

/**
 * Session-scoped container that maps slider input (0–100) into resistance multipliers
 * consumed by the Balls demo physics logic.
 */
class ToughnessControl(
    private val minimum: Int = MIN_VALUE,
    private val maximum: Int = MAX_VALUE,
    private val minEnergyTransfer: Float = MIN_ENERGY_TRANSFER,
    private val maxBreakThresholdMultiplier: Float = MAX_BREAK_THRESHOLD_MULTIPLIER,
    private val maxGlancingThresholdMultiplier: Float = MAX_GLANCING_THRESHOLD_MULTIPLIER,
    private val minReboundRetention: Float = MIN_REBOUND_RETENTION
) {

    var value: Int = DEFAULT_VALUE
        private set(value) {
            field = value.coerceIn(minimum, maximum)
        }

    private val normalized: Float
        get() = if (maximum == minimum) 0f else (value - minimum).toFloat() / (maximum - minimum).toFloat()

    private val effectNormalized: Float
        get() = (normalized * MAX_EFFECT_RATIO).coerceIn(0f, 1f)

    /**
     * Fraction of energy transferred into the body. High toughness drastically reduces the
     * usable energy, requiring significantly harder impacts to dislodge particles.
     */
    val energyTransferMultiplier: Float
        get() {
            val falloff = 1f - effectNormalized
            val falloffSquared = falloff * falloff
            return falloffSquared * (1f - minEnergyTransfer) + minEnergyTransfer
        }

    /**
     * Multiplier applied to the baseline break threshold. Increases non-linearly so the upper
     * end of the slider supplies markedly more resistance.
     */
    val breakThresholdMultiplier: Float
        get() = 1f + (maxBreakThresholdMultiplier - 1f) * effectNormalizedSquared

    /**
     * Multiplier for the glancing-impact threshold so lighter hits are absorbed at higher
     * toughness settings.
     */
    val glancingThresholdMultiplier: Float
        get() = 1f + (maxGlancingThresholdMultiplier - 1f) * effectNormalizedSquared

    /**
     * Fraction of projectile speed retained after impact. Higher toughness drains more speed,
     * making rebounds noticeably weaker.
     */
    val reboundRetentionFactor: Float
        get() = 1f + (minReboundRetention - 1f) * effectNormalizedSquared

    fun updateFromSlider(position: Float) {
        val unclamped = minimum + (position.coerceIn(0f, 1f) * (maximum - minimum).toFloat()).roundToInt()
        value = unclamped
    }

    fun reset() {
        value = DEFAULT_VALUE
    }

    fun sliderPosition(): Float = normalized

    companion object {
        const val MIN_VALUE = 0
        const val MAX_VALUE = 100
        const val DEFAULT_VALUE = 0

        private const val MAX_EFFECT_RATIO = 0.6f
        private const val MIN_ENERGY_TRANSFER = 0.03f
        private const val MAX_BREAK_THRESHOLD_MULTIPLIER = 35f
        private const val MAX_GLANCING_THRESHOLD_MULTIPLIER = 6f
        private const val MIN_REBOUND_RETENTION = 0.35f
    }

    private val effectNormalizedSquared: Float
        get() = effectNormalized * effectNormalized
}
