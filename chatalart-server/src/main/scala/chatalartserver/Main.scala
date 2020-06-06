package chatalartserver

import akka.actor.{ActorSystem, ClassicActorSystemProvider}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteResult
import akka.stream.{Materializer, SystemMaterializer}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

object Main extends App with MyExceptionHandler {

  println("chatalart-server: start")

  val conf = Utils.conf

  implicit val system: ActorSystem                = ActorSystem(conf.getString("actor.system.name "))
  implicit val materialiser: Materializer         = matFromSystem
  implicit val executionContext: ExecutionContext = system.dispatcher

  // Configuration of Server
  val host = conf.getString("http.host")
  val port = conf.getInt("http.port")

  val bindingFuture = Http()
    .bindAndHandle(RouteResult.route2HandlerFlow(Route.routes()), host, port)
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
    Await.result(system.whenTerminated, 10.seconds)
  }

  private def matFromSystem(implicit provider: ClassicActorSystemProvider): Materializer =
    SystemMaterializer(provider.classicSystem).materializer
}
