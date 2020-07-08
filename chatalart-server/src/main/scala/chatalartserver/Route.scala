package chatalartserver

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{ `Access-Control-Allow-Headers`, `Access-Control-Allow-Origin` }
import chatalartserver.model.AlartTargetRoom
import chatalartserver.usecase.alartonoff.{ AlartOffUsecase, AlartOnUsecase }
import chatalartserver.usecase.targets.TargetsUsecase
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

case class ErrorResponse(code: Int, message: String)

trait MyExceptionHandler {
  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: Throwable =>
        extractUri { uri =>
          println(s"Request to $uri could not be handled normally \n ${e.getStackTrace.mkString}")

          complete(
            ErrorResponse(
              InternalServerError.intValue,
              s"Request to $uri could not be handled normally.\n${e.getMessage}\n${e.getStackTrace.mkString})}"
            )
          )
        }
    }
}

object Route {

  def routes()(implicit system: ActorSystem, ec: ExecutionContext): Route = {
    extractUri { uri =>

      println(s"uri: $uri")

      respondWithHeaders(corsResponseHeaders) {
        path("public" / "html" / Remaining) { resource =>
          pathEndOrSingleSlash {
            get {
              getFromResource("frontend/public/html/" + resource)
            }
          }
        } ~
        path("public" / Remaining) { resource =>
          pathEndOrSingleSlash {
            get {
              getFromResource("frontend/public/" + resource)
            }
          }
        } ~
        path("targets") {
          val res = TargetsUsecase.getTargets()
          onSuccess(res) {
            case json =>
              println("成功：" + json)
              complete(json)
            case _ => throw new RuntimeException
          }
        } ~
        path("alartswitch") {
          post {
            entity(as[AlartTargetRoom]) { json: AlartTargetRoom =>
            println(json)
              if (json.isChecked) {
                AlartOnUsecase.alartOn(json.roomId)
                complete(AlartSwitchResponse("Alart On"))
              } else {
                AlartOffUsecase.alartOff(json.roomId)
                complete(AlartSwitchResponse("Alart Off"))
              }
            }
          }
        }
      }
    }
  }

  case class AlartSwitchResponse(result: String)

  private val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With")
  )
}
