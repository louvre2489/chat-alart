package chatalartserver.usecase.targets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import chatalartserver.http.ApiTokenHeader
import chatalartserver.model.Room
import chatalartserver.model.RoomDecoder._
import chatalartserver.Utils

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object TargetsUsecase {

  def getTargets()(implicit system: ActorSystem, ec: ExecutionContext): Future[List[Room]] = {
    val req = HttpRequest(
      method = HttpMethods.GET,
      uri = Utils.chatDomain + "rooms",
    ).addHeader(ApiTokenHeader(Utils.token))

    val response = Http().singleRequest(req)

    response.map { v =>
      val body: Future[String] = Unmarshal(v.entity).to[String]
      val json                 = Await.result(body, Duration.Inf)

      val rooms = decodeRooms(json).getOrElse(List())
      rooms.filter(r => r.sticky).sortBy(r => r.lastUpdateTime * -1)
    }
  }
}
