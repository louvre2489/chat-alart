package chatalartserver.store

import java.util.Properties

import akka.Done

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.kafka.{ ProducerMessage, ProducerSettings }
import akka.stream.KillSwitches
import akka.stream.scaladsl.{ Keep, Sink, Source }
import chatalartserver.model.Message
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object MessageStore {

  private val topic = "updatedChat"

  implicit val system: ActorSystem = ActorSystem("kafka")
  private val producerSettings     = createProducerSettings()

  def store(chatName: String, message: Message)(implicit system: ActorSystem, ec: ExecutionContext): Unit = {

    val done: Future[Done] =
      Source
        .single(message)
        .map { _ =>
          new ProducerRecord[String, String](topic, s"${chatName}に${message.account.name}さんからメッセージが届きました")
        }
        .runWith(Producer.plainSink(producerSettings))

    done.onComplete {
      case Success(_) =>
        println("Finish Send Message!")
      case Failure(ex) =>
        println(s"Fail Send. Reason: ${ex.getMessage}")
    }
  }

  private def createProducerSettings(): ProducerSettings[String, String] = {
    val config = system.settings.config.getConfig("akka.kafka.producer")

    ProducerSettings(config, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")
  }
}
