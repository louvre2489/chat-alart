package chatalartserver.usecase.alartonoff

import java.util.concurrent.Executors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, StatusCodes }
import akka.http.scaladsl.unmarshalling.Unmarshal
import chatalartserver.Utils
import chatalartserver.http.ApiTokenHeader
import chatalartserver.model.MessageDecoder._
import chatalartserver.usecase.alartonoff.AlartActor.WatchRoomId

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

object AlartOnUsecase {
  def alartOn(roomId: Long): Unit = {

    // Actor内でThread.sleepしてチェックのペースを制御するため、ONにするごとに専用のスレッドを作成する
    implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

    val system = ActorSystem(AlartActor(roomId), s"alart-${roomId.toString}")
    system ! WatchRoomId(roomId)
  }
}

object AlartActor {

  final case class WatchRoomId(roomId: Long)

  def apply(roomId: Long)(implicit ec: ExecutionContext): Behavior[WatchRoomId] = {
    println(s"Watch Room: ${roomId.toString}")

    Behaviors.receive { (context, message) =>
      // API経由で最新メッセージを取得する(force=0)

      implicit val system = context.system.classicSystem

      val req = HttpRequest(
        method = HttpMethods.GET,
        uri = Utils.chatDomain + "rooms/" + roomId.toString + "/messages?force=0",
      ).addHeader(ApiTokenHeader(Utils.token))

      Http()
        .singleRequest(req)
        .map { v =>
          v.status match {
            case StatusCodes.OK =>
              // 取得コンテンツがあればKafkaにメッセージ内容を積む
              val body: Future[String] = Unmarshal(v.entity).to[String]
              val json                 = Await.result(body, Duration.Inf)

              println("レスポンス：")
              println(json)

              // [{"message_id":"1234567891234567890","account":{"account_id":1235,"name":"名前","avatar_image_url":"https://appdata.chatwork.com/avatar/1234/dummy.rsz.png"},"body":"test","send_time":1594128616,"update_time":0}]
              val result = decodeMessages(json).getOrElse(List())
              println(result)
              result
            case StatusCodes.NoContent =>
              println(s"ルーム${roomId.toString}の新規メッセージなし")
              List()
            case _ =>
              println(s"想定外のレスポンス - レスポンスコード：${v.status} - エンティティ：${v.entity}")
              List()
          }
        }.recover { e =>
          println(e)
          throw e
        }

      // 10秒待って再度自分自身を呼び出す
      Thread.sleep(10000)
      context.self ! message

      Behaviors.same
    }
  }
}
