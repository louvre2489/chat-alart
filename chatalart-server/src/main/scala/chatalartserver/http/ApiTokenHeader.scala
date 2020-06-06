package chatalartserver.http

import akka.http.scaladsl.model.headers.{ ModeledCustomHeader, ModeledCustomHeaderCompanion }

import scala.util.Try

final class ApiTokenHeader(token: String) extends ModeledCustomHeader[ApiTokenHeader] {
  override def renderInRequests               = true
  override def renderInResponses              = true
  override val companion: ApiTokenHeader.type = ApiTokenHeader
  override def value: String                  = token
}
object ApiTokenHeader extends ModeledCustomHeaderCompanion[ApiTokenHeader] {
  override val name                                      = "X-ChatWorkToken"
  override def parse(value: String): Try[ApiTokenHeader] = Try(new ApiTokenHeader(value))
}
