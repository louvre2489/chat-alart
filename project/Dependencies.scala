import sbt._

object Dependencies {

  object Logback {
    val version           = "1.2.3"
    val classic: ModuleID = "ch.qos.logback" % "logback-classic" % version
  }

  object Akka {
    val akkaVersion     = "2.6.5"
    val akkaHttpVersion = "10.1.12"
    val actor           = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
    val http            = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val slf4j           = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    val testkit         = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
    val httpTestkit     = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
  }

  object Circe {
    val version           = "0.12.3"
    val core: ModuleID    = "io.circe" %% "circe-core" % version
    val parser: ModuleID  = "io.circe" %% "circe-parser" % version
    val generic: ModuleID = "io.circe" %% "circe-generic" % version
  }

  object ScalaTest {
    val version             = "3.1.2"
    val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % version
  }
}
