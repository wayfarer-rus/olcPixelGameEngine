package game.vermis

import olc.game_engine.PixelGameEngine

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

  fun initLayers(e: PixelGameEngine) {
    layers[Layer.BEHIND_BACKGROUND] = e.createLayer()
    layers[Layer.BACKGROUND] = e.createLayer()
    layers[Layer.INTERACTABLE] = e.createLayer()
    layers[Layer.PLAYER] = e.createLayer()
    layers[Layer.FOREGROUND] = e.createLayer()
    layers[Layer.OVERLAY_DEBUG] = e.createLayer()
  }

  override val size: Int = layers.size
  override val keys: Set<Layer> = layers.keys
  override val values: Collection<Int> = layers.values
  override val entries: Set<Map.Entry<Layer, Int>> = layers.entries

  override fun isEmpty(): Boolean = layers.isEmpty()
  override fun containsKey(key: Layer): Boolean = layers.containsKey(key)
  override fun containsValue(value: Int): Boolean = layers.containsValue(value)
  override fun get(key: Layer): Int = layers[key]!!
}
