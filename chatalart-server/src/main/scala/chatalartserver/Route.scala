package chatalartserver

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{ `Access-Control-Allow-Headers`, `Access-Control-Allow-Origin` }
import chatalartserver.model.AlartTargetRoom
import chatalartserver.targets.TargetsUsecase
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }

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
      println("uri:" + uri)
      addAccessControlHeaders {
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
            extractRequestEntity { r =>
              entity(as[AlartTargetRoom]) { json: AlartTargetRoom =>
                println(json)
                val resultFuture: Future[String] = ???
                onSuccess(resultFuture) {
                  case s: String =>
                    val response = AlartResponse("OK")
                    complete(json)
                  case _ => throw new Exception()
                }
              }
            }
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
  }

  case class AlartResponse(result: String)

  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(corsResponseHeaders)
  }

  private val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With")
  )
}
