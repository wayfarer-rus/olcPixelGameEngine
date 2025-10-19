package demos.balls

import demos.slider.Slider
import geometry_2d.Point
import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@ExperimentalUnsignedTypes
class BallsDemo : PixelGameEngineImpl() {

    private val structureConfig = StructureConfiguration()
    private val projectileSettings = ProjectileSettings()
    private val toughnessControl = ToughnessControl()
    private val structurePresentation = ControlPresentation(
        theme = ControlPalette.structure,
        valueBadge = ControlPresentation.structureBadge(structureConfig.currentSize)
    )
    private val massPresentation = ControlPresentation(
        theme = ControlPalette.mass,
        valueBadge = projectileSettings.badgeFor()
    )
    private val toughnessPresentation = ControlPresentation(
        theme = ControlPalette.toughness,
        valueBadge = toughnessControl.value.toString()
    )
    private val toughnessDefaultPosition = toughnessControl.sliderPosition()
    private val panelBackground = Pixel(24, 24, 24)
    private val panelBorder = Pixel(55, 55, 55)
    private val panelPaddingX = 8
    private val panelPaddingY = 10
    private val sliderSpacing = 26
    private lateinit var structureSlider: Slider
    private lateinit var massSlider: Slider
    private lateinit var toughnessSlider: Slider

    override fun onUserCreate(): Boolean {
        reset(initial = true)
        offset = Point(
            getDrawTargetWidth() / 2 - body.w * R + R,
            getDrawTargetHeight() / 2 - body.h * R + R
        )
        structureSlider = Slider(
            engine = this,
            length = 160,
            width = 10,
            initialPosition = structureConfig.sliderPosition()
        )
        massSlider = Slider(
            engine = this,
            length = 160,
            width = 10,
            initialPosition = projectileSettings.sliderPosition()
        )
        toughnessSlider = Slider(
            engine = this,
            length = 160,
            width = 10,
            initialPosition = toughnessControl.sliderPosition()
        )
        return true
    }

    private fun reset(initial: Boolean = false) {
        if (initial) {
            structureConfig.reset()
            projectileSettings.reset()
        } else {
            structureConfig.applyPendingIfPresent()
            projectileSettings.applyPendingIfPresent()
        }

        rebuildBody()
        projectiles.clear()
        splats.clear()
        toughnessControl.reset()
        if (this::toughnessSlider.isInitialized) {
            toughnessSlider.isHeld = false
            toughnessSlider.position = toughnessControl.sliderPosition()
        }
        if (this::structureSlider.isInitialized) {
            structureSlider.isHeld = false
            structureSlider.position = structureConfig.sliderPosition()
        }
        if (this::massSlider.isInitialized) {
            massSlider.isHeld = false
            massSlider.position = projectileSettings.sliderPosition()
        }
        structurePresentation.valueBadge = ControlPresentation.structureBadge(structureConfig.currentSize)
        massPresentation.valueBadge = projectileSettings.badgeFor()
        toughnessPresentation.valueBadge = toughnessControl.value.toString()
    }

    private val baseProjectileMass = 1.0f

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        val gunPos = Point(0, getDrawTargetHeight() / 2)

        if (getMouseKey(1).bPressed) {
            if (projectileSettings.hasPendingChange()) {
                if (projectileSettings.applyPendingIfPresent()) {
                    if (this::massSlider.isInitialized && !massSlider.isHeld) {
                        massSlider.position = projectileSettings.sliderPosition()
                    }
                }
            }
            val massMultiplier = projectileSettings.currentMultiplier
            val projectileMass = baseProjectileMass * massMultiplier
            val initialVelocity = (Point(getMouseX(), getMouseY()) - gunPos).toLength(speed)
            val p = Projectile(gunPos, initialVelocity, projectileMass, speed)
            projectiles.add(p)
        }

        if (getKey(Key.R).bPressed) {
            reset()
        }

        clear()
        drawControlPanel()

        val fragmentsToSpawn: MutableList<Projectile> = mutableListOf()
        val partsToRemove: MutableSet<Point> = mutableSetOf()

        val breakThreshold = baseEnergyThreshold * toughnessControl.breakThresholdMultiplier
        val glancingThreshold = MIN_GLANCING_ENERGY * toughnessControl.glancingThresholdMultiplier

        projectiles.forEach { projectile ->
            projectile.move(elapsedTime)
            drawCircle(projectile.pos.xi, projectile.pos.yi, R)

            val impactedParts = body.parts.filter { part ->
                val worldPos = offset + part
                Point.distance(projectile.pos, worldPos) <= 2 * R
            }

            impactedParts.forEach { part ->
                val worldPart = offset + part
                val u = projectile.mass * projectile.speed / (projectile.mass + body.partMass)
                val k = body.partMass * u.pow(2) / 2
                val massFactor = (projectile.mass / baseProjectileMass).coerceAtLeast(0.1f)
                val effectiveEnergy =
                    k * massFactor.pow(1.25f) * toughnessControl.energyTransferMultiplier
                val reboundSpeed = projectile.speed * toughnessControl.reboundRetentionFactor
                val hardImpact = effectiveEnergy > glancingThreshold

                when {
                    effectiveEnergy > breakThreshold -> {
                        projectile.collided = true
                        val transferredSpeed =
                            (u * toughnessControl.energyTransferMultiplier).coerceAtLeast(MIN_FRAGMENT_SPEED)
                        val fragmentSpeedBoost = sqrt(massFactor.toDouble()).toFloat()
                        val fragment = Projectile(
                            worldPart,
                            (worldPart - projectile.pos).toLength(transferredSpeed * fragmentSpeedBoost),
                            body.partMass,
                            transferredSpeed * fragmentSpeedBoost
                        ).also { it.collided = true }
                        projectile.speed = reboundSpeed
                        projectile.v = (projectile.pos - worldPart).toLength(reboundSpeed)

                        fragmentsToSpawn += fragment
                        val blastRadius = 2f * R * massFactor
                        body.parts.forEach { candidate ->
                            if (Point.distance(candidate, part) <= blastRadius) {
                                if (partsToRemove.add(candidate)) {
                                    val candidateWorld = offset + candidate
                                    val direction = (candidateWorld - projectile.pos).toLength(
                                        (transferredSpeed * fragmentSpeedBoost).coerceAtLeast(MIN_FRAGMENT_SPEED)
                                    )
                                    val debris = Projectile(
                                        candidateWorld,
                                        direction,
                                        body.partMass,
                                        transferredSpeed * fragmentSpeedBoost
                                    ).also { it.collided = true }
                                    fragmentsToSpawn += debris
                                }
                            }
                        }
                    }

                    hardImpact -> {
                        projectile.collided = true
                        projectile.speed = reboundSpeed
                        projectile.v = (projectile.pos - worldPart).toLength(reboundSpeed)
                    }

                    else -> {
                        projectile.collided = false
                        projectile.speed = reboundSpeed
                        projectile.v = (projectile.pos - worldPart).toLength(reboundSpeed)
                    }
                }
            }
        }

        if (partsToRemove.isNotEmpty()) {
            body.parts.removeAll(partsToRemove)
        }
        if (fragmentsToSpawn.isNotEmpty()) {
            projectiles.addAll(fragmentsToSpawn)
        }

        splats.forEach {
            it.move(elapsedTime)
            drawCircle(it.pos.xi, it.pos.yi, R)
        }

        splats.removeAll { it.v.x < 1.0 || it.v.y < 1.0 }

        projectiles.removeAll { it.pos.x < 0 || it.pos.y < 0 || it.pos.x > getDrawTargetWidth() || it.pos.y > getDrawTargetHeight() }

        body.parts.forEach {
            val p = offset + it
            drawCircle(p.xi, p.yi, R)
        }

        val gunEndPoint = (Point(getMouseX(), getMouseY()) - gunPos).toLength(10f) + gunPos
        drawLine(gunPos.toPair(), gunEndPoint.toPair())

        fillCircle(getMouseX(), getMouseY(), 1)
        return true
    }

    private fun drawControlPanel() {
        val sliderX = 28
        val structureSliderY = getDrawTargetHeight() - 72
        val massSliderY = structureSliderY + sliderSpacing
        val toughnessSliderY = massSliderY + sliderSpacing

        if (this::structureSlider.isInitialized) {
            drawStructureControl(sliderX, structureSliderY)
        }
        if (this::massSlider.isInitialized) {
            drawMassControl(sliderX, massSliderY)
        }
        if (this::toughnessSlider.isInitialized) {
            drawToughnessControl(sliderX, toughnessSliderY)
        }
    }

    private fun drawStructureControl(baseX: Int, sliderY: Int) {
        structureSlider.draw(baseX, sliderY, Pixel.BLANK)
        structureConfig.queueFromSlider(structureSlider.position)
        if (structureConfig.hasPendingChange() && isSimulationIdle()) {
            if (structureConfig.applyPendingIfPresent()) {
                rebuildBody()
            }
        } else if (!structureConfig.hasPendingChange() && !structureSlider.isHeld) {
            val snapped = structureConfig.sliderPosition()
            if (structureSlider.position != snapped) {
                structureSlider.position = snapped
            }
        }
        val displaySize = structureConfig.sizeFor(structureSlider.position)
        structurePresentation.valueBadge = ControlPresentation.structureBadge(displaySize)
        drawSliderUI(
            slider = structureSlider,
            presentation = structurePresentation,
            baseX = baseX,
            sliderY = sliderY,
            valueText = structurePresentation.valueBadge,
            defaultPosition = structureConfig.sliderPositionFor(structureConfig.defaultSize)
        )
    }

    private fun drawMassControl(baseX: Int, sliderY: Int) {
        massSlider.draw(baseX, sliderY, Pixel.BLANK)
        projectileSettings.queueFromSlider(massSlider.position)
        if (projectileSettings.hasPendingChange() && isSimulationIdle()) {
            if (projectileSettings.applyPendingIfPresent() && !massSlider.isHeld) {
                massSlider.position = projectileSettings.sliderPosition()
            }
        } else if (!projectileSettings.hasPendingChange() && !massSlider.isHeld) {
            val snapped = projectileSettings.sliderPosition()
            if (massSlider.position != snapped) {
                massSlider.position = snapped
            }
        }
        val badge = projectileSettings.badgeFor(projectileSettings.multiplierFor(massSlider.position))
        massPresentation.valueBadge = badge
        drawSliderUI(
            slider = massSlider,
            presentation = massPresentation,
            baseX = baseX,
            sliderY = sliderY,
            valueText = massPresentation.valueBadge,
            defaultPosition = projectileSettings.sliderPositionFor(projectileSettings.defaultMultiplier)
        )
    }

    private fun drawToughnessControl(baseX: Int, sliderY: Int) {
        toughnessSlider.draw(baseX, sliderY, Pixel.BLANK)
        toughnessControl.updateFromSlider(toughnessSlider.position)
        toughnessPresentation.valueBadge = toughnessControl.value.toString()
        drawSliderUI(
            slider = toughnessSlider,
            presentation = toughnessPresentation,
            baseX = baseX,
            sliderY = sliderY,
            valueText = toughnessPresentation.valueBadge,
            defaultPosition = toughnessDefaultPosition
        )
    }

    private fun drawSliderUI(
        slider: Slider,
        presentation: ControlPresentation,
        baseX: Int,
        sliderY: Int,
        valueText: String,
        defaultPosition: Float
    ) {
        val panelX = baseX - panelPaddingX
        val panelY = sliderY - panelPaddingY
        val panelWidth = slider.length + 120
        val panelHeight = panelPaddingY * 2
        fillRect(panelX, panelY, panelWidth, panelHeight, panelBackground)
        drawRect(panelX, panelY, panelWidth, panelHeight, panelBorder)

        val trackStart = sliderTrackStart(baseX, slider)
        val trackEnd = sliderTrackEnd(baseX, slider)
        fillRect(trackStart, sliderY - 1, trackEnd - trackStart, 3, presentation.theme.trackColor.pixel)

        val markerX = sliderMarkerX(slider, baseX, defaultPosition)
        drawLine(markerX to (sliderY - 5), markerX to (sliderY + 5), presentation.theme.markerColor.pixel)

        val block = slider.getBlockPosition(baseX, sliderY, Slider.Orientation.HORIZONTAL)
        val handleCenterX = block.first.x + block.second.x / 2f
        val handleHalf = slider.width / 2
        fillRect(
            (handleCenterX - handleHalf).roundToInt(),
            sliderY - handleHalf,
            handleHalf * 2,
            handleHalf * 2,
            presentation.theme.handleColor.pixel
        )

        drawString(panelX + 2, panelY + 2, presentation.theme.label, presentation.theme.textColor.pixel)
        drawString(trackEnd + 12, panelY + 2, valueText, presentation.theme.textColor.pixel)
    }

    private fun sliderTrackStart(baseX: Int, slider: Slider): Int =
        baseX + slider.width / 2

    private fun sliderTrackEnd(baseX: Int, slider: Slider): Int =
        baseX + slider.length - slider.width / 2

    private fun rebuildBody() {
        body = Body(structureConfig.currentSize, structureConfig.currentSize, 64, R)
        offset = Point(
            getDrawTargetWidth() / 2 - body.w * R + R,
            getDrawTargetHeight() / 2 - body.h * R + R
        )
    }

    private fun isSimulationIdle(): Boolean = projectiles.isEmpty() && splats.isEmpty()

    private fun sliderMarkerX(slider: Slider, baseX: Int, position: Float): Int {
        val halfWidth = slider.width / 2f
        val travel = slider.length - 3 * halfWidth
        val blockWidth = slider.width / 2f
        val start = baseX + halfWidth + travel * position
        val center = start + blockWidth / 2f
        return center.roundToInt()
    }

    private val R = 1
    private lateinit var body: Body
    private var offset = Point(0, 0)
    private val speed = 400f
    private val baseEnergyThreshold = 75f
    private val projectiles: MutableList<Projectile> = mutableListOf()
    private val splats: MutableList<Projectile> = mutableListOf()

    companion object {
        private const val MIN_GLANCING_ENERGY = 1.0f
        private const val MIN_FRAGMENT_SPEED = 5f
    }
}

class Projectile(var pos: Point, var v: Point, val mass: Float, var speed: Float) {
    var collided = false
    private var lifetime = 1.0f
    fun move(elapsedTime: Float) {
        if (collided) lifetime -= elapsedTime

        if (lifetime < 0.0) pos = Point(-100, -100)
        else pos += v * elapsedTime * lifetime
    }
}

class Body(val w: Int, val h: Int, val mass: Int, radius: Int) {
    val parts: MutableList<Point> = mutableListOf()
    val partMass: Float = mass.toFloat() / (w * h).toFloat()

    init {
        val R = radius
        for (j in 0 until h) {
            for (i in 0 until w) {
                val p = Point(i * 2 * R, j * 2 * R)
                this.parts.add(p)
            }
        }
    }
}

@ExperimentalUnsignedTypes
fun main() {
    val demo = BallsDemo()
    if (demo.construct() == RetCode.OK) demo.start()
}
