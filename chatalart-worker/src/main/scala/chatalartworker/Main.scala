package chatalartworker

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ ConsumerSettings, Subscriptions }
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.sys.process.Process
import scala.util.{ Failure, Success }

object Main extends App {

  println("chatalart-worker: start")

  private val topic = "updatedChat"

  implicit val system: ActorSystem                = ActorSystem("kafka")
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val consumerSettings = createConsumerSettings()

  private val totalMessages = 10
  private val lastMessage   = Promise[Done]

  val control: DrainingControl[immutable.Seq[Done]] =
    Consumer
      .atMostOnceSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(1) { record =>
        println("メッセージを受け取りました")
        business(record.key, record.value())
      }
      .toMat(Sink.seq)(DrainingControl.apply)
      .run()

  lastMessage.future.onComplete {
    case Success(_) =>
      println("Finish Receive Message!")
    case Failure(ex) =>
      println(s"Fail Receive. Reason: ${ex.getMessage}")
  }

  private def createConsumerSettings(): ConsumerSettings[String, String] = {
    val config = system.settings.config.getConfig("akka.kafka.consumer")

    ConsumerSettings(config, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("chatalart")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
  }

  private def business(key: String, value: String): Future[Done] = {
    // 音声ファイルの作成
    println(s"音声ファイルを作成:$value")
    Process(s"echo $value | docker run -i --rm u6kapps/open_jtalk > chatalart.wav").run()
    // 音声ファイルの再生
    println("音声ファイルを再生")
    Process("aplay chatalart.wav").run()
    // 削除
    println("音声ファイルを削除")
    Process("rm -f chatalart.wav").run()

    if (value.toList == totalMessages.toString.getBytes.toList) lastMessage.success(Done)
    Future.successful(Done)
  }
}
