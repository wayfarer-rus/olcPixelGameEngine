package game.vermis

import olc.game_engine.PixelGameEngine
import olc.game_engine.Sprite

enum class Layer {
  BEHIND_BACKGROUND,
  BACKGROUND,
  INTERACTABLE,
  PLAYER,
  FOREGROUND,
  OVERLAY_DEBUG
}

object LayersMap: Map<Layer, Int> {
  private val layers = mutableMapOf<Layer, Int>()
  private val sprites = mutableMapOf<Layer, Sprite>()

  fun initLayers(e: PixelGameEngine) {
    for (layer in Layer.entries) {
      val id = e.createLayer()
      layers[layer] = id
      e.setDrawTarget(id)
      sprites[layer] = e.getDrawTarget()
    }
  }

  fun sprite(layer: Layer): Sprite = sprites[layer]!!

  override val size: Int get() = layers.size
  override val keys: Set<Layer> get() = layers.keys
  override val values: Collection<Int> get() = layers.values
  override val entries: Set<Map.Entry<Layer, Int>> get() = layers.entries

  override fun isEmpty(): Boolean = layers.isEmpty()
  override fun containsKey(key: Layer): Boolean = layers.containsKey(key)
  override fun containsValue(value: Int): Boolean = layers.containsValue(value)
  override fun get(key: Layer): Int = layers[key]!!
}
