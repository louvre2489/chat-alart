package chatalartserver.model

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._

final case class Message(
    messageId: String,
    account: Account,
    body: String
)

object MessageDecoder {

  implicit val encodeAccount: Encoder[Account]        = AccountDecoder.encodeAccount
  implicit val encodeMessage: Encoder[Message]        = deriveEncoder[Message]
  implicit val encodeMessages: Encoder[List[Message]] = Encoder.encodeList[Message]

  def decodeMessages(json: String): Option[List[Message]] = {
    val jsonObject: Json = parse(json).getOrElse(Json.Null)
    val hCursor: HCursor = jsonObject.hcursor

    val rooms = for (jsons <- hCursor.values) yield {
      for (json <- jsons.toList) yield {
        val messageId: String = json.hcursor.get[String]("message_id").getOrElse("0")
        val account: Account  = AccountDecoder.decodeAccount(json.hcursor.get[Json]("account").getOrElse(Json.True))
        val body: String      = json.hcursor.get[String]("body").getOrElse("")

        Message(messageId, account, body)
      }
    }

    rooms
  }
}
