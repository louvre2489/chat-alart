package chatalartserver.model

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._

final case class Room(roomId: Long,
                      name: String,
                      sticky: Boolean,
                      iconPath: String,
                      lastUpdateTime: Long,
                      isChecked: Boolean)

object RoomDecoder {

  implicit val encodeRoom: Encoder[Room]        = deriveEncoder[Room]
  implicit val encodeRooms: Encoder[List[Room]] = Encoder.encodeList[Room]

  def decodeRooms(json: String): Option[List[Room]] = {
    val jsonObject: Json = parse(json).getOrElse(Json.Null)
    val hCursor: HCursor = jsonObject.hcursor

    val rooms = for (jsons <- hCursor.values) yield {
      for (json <- jsons.toList) yield {
        val roomId: Long         = json.hcursor.get[Long]("room_id").getOrElse(0)
        val name: String         = json.hcursor.get[String]("name").getOrElse("")
        val sticky: Boolean      = json.hcursor.get[Boolean]("sticky").getOrElse(false)
        val iconPath: String     = json.hcursor.get[String]("icon_path").getOrElse("")
        val lastUpdateTime: Long = json.hcursor.get[Long]("last_update_time").getOrElse(0)

        Room(roomId, name, sticky, iconPath, lastUpdateTime, false)
      }
    }

    rooms
  }
}
