package chatalartserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteResult
import com.typesafe.config.ConfigFactory

import akka.stream.ActorMaterializer
import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

import chatalartserver.Route.exceptionHandler

object Main extends App {

  println("chatalart-server: start")

  val conf = ConfigFactory.load

  implicit val system                             = ActorSystem(conf.getString("actor.system.name "))
  implicit val materialiser                       = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  // Configuration of Server
  val host = conf.getString("http.host")
  val port = conf.getInt("http.port")

  val bindingFuture = Http()
    .bindAndHandle(RouteResult.route2HandlerFlow(Route.routes), host, port)
    .map { binding =>
      println(s"Server online at https://$host:$port")
      binding
    }

  sys.addShutdownHook {
    for {
      binding <- bindingFuture
      _       <- binding.unbind()
    } yield {
      materialiser.shutdown()
      system.terminate()
    }
    Await.result(system.whenTerminated, 10 seconds)
  }
}
