package chatalartserver.model

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._

final case class Account(
    account_id: Long,
    name: String
)

object AccountDecoder {

  implicit val encodeAccount: Encoder[Account]             = deriveEncoder[Account]
  implicit val encodeOptionAccount: Encoder[Some[Account]] = Encoder.encodeSome[Account]

  def decodeAccount(jsonObject: Json): Account = {

    val accountId: Long = jsonObject.hcursor.get[Long]("account_id").getOrElse(0)
    val name: String    = jsonObject.hcursor.get[String]("name").getOrElse("名無しの権兵衛")

    Account(accountId, name)
  }
}
