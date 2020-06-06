package chatalartserver.model

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._

case class Room(room_id: Long,
                name: String,
                `type`: String,
                role: String,
                sticky: Boolean,
                unread_num: Long,
                mention_num: Long,
                mytask_num: Long,
                message_num: Long,
                file_num: Long,
                task_num: Long,
                icon_path: String,
                last_update_time: Long)

object RoomEncDec {

  implicit val encodeRoom: Encoder[Room] = deriveEncoder[Room]
  implicit val encodeRooms: Encoder[List[Room]] = Encoder.encodeList[Room]

  def decodeRooms(json: String): Option[List[Room]] = {
    val jsonObject: Json = parse(json).getOrElse(Json.Null)
    val hCursor: HCursor = jsonObject.hcursor

    val rooms = for (jsons <- hCursor.values) yield {
      for (json <- jsons.toList) yield {
        val room_id: Long          = json.hcursor.get[Long]("room_id").getOrElse(0)
        val name: String           = json.hcursor.get[String]("name").getOrElse("")
        val `type`: String         = json.hcursor.get[String]("type").getOrElse("")
        val role: String           = json.hcursor.get[String]("role").getOrElse("")
        val sticky: Boolean        = json.hcursor.get[Boolean]("sticky").getOrElse(false)
        val unread_num: Long       = json.hcursor.get[Long]("unread_num").getOrElse(0)
        val mention_num: Long      = json.hcursor.get[Long]("mention_num").getOrElse(0)
        val mytask_num: Long       = json.hcursor.get[Long]("mytask_num").getOrElse(0)
        val message_num: Long      = json.hcursor.get[Long]("message_num").getOrElse(0)
        val file_num: Long         = json.hcursor.get[Long]("file_num:").getOrElse(0)
        val task_num: Long         = json.hcursor.get[Long]("task_num").getOrElse(0)
        val icon_path: String      = json.hcursor.get[String]("icon_path").getOrElse("")
        val last_update_time: Long = json.hcursor.get[Long]("last_update_time").getOrElse(0)

        Room(room_id,
             name,
             `type`,
             role,
             sticky,
             unread_num,
             mention_num,
             mytask_num,
             message_num,
             file_num,
             task_num,
             icon_path,
             last_update_time)
      }
    }

    rooms
  }
}

