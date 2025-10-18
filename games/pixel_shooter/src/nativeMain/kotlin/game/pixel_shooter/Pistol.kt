package game.pixel_shooter

interface Gun {
    val projectileVelocity: Float

    fun fire(elapsedTime: Float): Boolean
}

class Pistol(override val projectileVelocity: Float = 40.0f) : Gun {
    override fun fire(elapsedTime: Float): Boolean {
        return true
    }

}
