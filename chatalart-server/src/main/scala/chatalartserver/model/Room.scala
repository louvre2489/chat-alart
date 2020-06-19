package chatalartserver.model

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._

case class Room(room_id: Long,
                name: String,
                sticky: Boolean,
                icon_path: String,
                last_update_time: Long,
                isChecked: Boolean)

object RoomEncDec {

  implicit val encodeRoom: Encoder[Room]        = deriveEncoder[Room]
  implicit val encodeRooms: Encoder[List[Room]] = Encoder.encodeList[Room]

  def decodeRooms(json: String): Option[List[Room]] = {
    val jsonObject: Json = parse(json).getOrElse(Json.Null)
    val hCursor: HCursor = jsonObject.hcursor

    val rooms = for (jsons <- hCursor.values) yield {
      for (json <- jsons.toList) yield {
        val room_id: Long          = json.hcursor.get[Long]("room_id").getOrElse(0)
        val name: String           = json.hcursor.get[String]("name").getOrElse("")
        val sticky: Boolean        = json.hcursor.get[Boolean]("sticky").getOrElse(false)
        val icon_path: String      = json.hcursor.get[String]("icon_path").getOrElse("")
        val last_update_time: Long = json.hcursor.get[Long]("last_update_time").getOrElse(0)

        Room(room_id, name, sticky, icon_path, last_update_time, false)
      }
    }

    rooms
  }
}
