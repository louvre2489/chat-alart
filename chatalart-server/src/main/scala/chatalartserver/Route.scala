package chatalartserver

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import akka.actor.ActorSystem
import chatalartserver.targets.TargetsUsecase
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
    path("targets") {
      val res = TargetsUsecase.getTargets()
      onSuccess(res) {
        case json => complete(json)
        case _ => throw new RuntimeException
      }
    } ~
    path("list") {
      complete("OK")
    } ~
    path("alarton") {
      throw new Exception
    }

  }
}
