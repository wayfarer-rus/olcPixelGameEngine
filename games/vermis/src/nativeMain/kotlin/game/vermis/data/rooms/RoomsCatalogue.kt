package game.vermis.data.rooms

import game.vermis.data.rooms.greengrave.GreenGrave
import olc.game_engine.PixelGameEngine

object RoomsCatalogue {

  fun initRooms(e: PixelGameEngine) {
    GreenGrave.initRooms(e)
  }
}
