package chatalartserver

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

case class ErrorResponse(code: Int, message: String)

object Route {

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

  def routes: Route = {
    path("list") {
      complete("OK")
    } ~
    path("alarton") {
      throw new Exception
    }

  }
}
