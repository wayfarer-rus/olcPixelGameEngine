package geometry_2d

import olc.game_engine.Pixel
import olc.game_engine.Sprite

data class BoundingBox(val x: Float, val y: Float, val w: Float, val h: Float) {
  val left get() = x
  val top get() = y
  val right get() = x + w
  val bottom get() = y + h

  fun overlaps(other: BoundingBox): Boolean =
    left < other.right && right > other.left && top < other.bottom && bottom > other.top
}

/**
 * Check if a bounding box overlaps any non-BLANK pixel on the given sprite.
 * Samples corners + edge midpoints (8 points).
 * Out-of-bounds = collision (blocks movement beyond sprite/room boundaries).
 */
fun collidesWithSprite(box: BoundingBox, sprite: Sprite): Boolean {
  val points = listOf(
    box.left to box.top,
    box.right - 1 to box.top,
    box.left to box.bottom - 1,
    box.right - 1 to box.bottom - 1,
    (box.left + box.w / 2) to box.top,
    (box.left + box.w / 2) to (box.bottom - 1),
    box.left to (box.top + box.h / 2),
    (box.right - 1) to (box.top + box.h / 2),
  )
  for ((px, py) in points) {
    val ix = px.toInt()
    val iy = py.toInt()
    if (ix < 0 || iy < 0 || ix >= sprite.width || iy >= sprite.height) return true
    if (sprite.getPixel(ix, iy) != Pixel.BLANK) return true
  }
  return false
}
