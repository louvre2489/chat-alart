import Dependencies._
import sbt._
import sbt.Keys._
import ScalafixPlugin.autoImport._

// sbt コマンドエイリアス
addCommandAlias("fixCheck", "; scalafixEnable; compile:scalafix --check")
addCommandAlias("fixTestCheck", "; scalafixEnable; test:scalafix --check")
addCommandAlias("fixAllCheck", "; scalafixEnable; scalafix --check ; test:scalafix --check")

version := "0.1"

lazy val chatalartServer = (project in file("chatalart-server"))
  .settings(commonSettings)
  .settings(dockerCommonSettings)
  .settings(scalafixCommonSettings)
  .settings(
    name := "chatalart-server",
    fork in run := true,
   )
  .settings(
     libraryDependencies ++= Seq(
       Logback.classic,
       Akka.slf4j,
       Akka.actor,
       Akka.http,
       Akka.stream,
       Akka.kafka,
       Circe.generic,
       AkkaHttpCirce.akkahttpCirce,
       ScalaTest.scalaTest % "test"
     )
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val chatalartWorker = (project in file("chatalart-worker"))
  .settings(commonSettings)
  .settings(dockerCommonSettings)
  .settings(scalafixCommonSettings)
  .settings(
    name := "chatalart-worker",
    fork in run := true,
  )
  .settings(
     libraryDependencies ++= Seq(
       Logback.classic,
       Akka.actor,
       Akka.stream,
       Akka.kafka,
       ScalaTest.scalaTest % "test"
     )
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(AshScriptPlugin)

lazy val commonSettings = Seq (
  scalaVersion := "2.13.2",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-encoding",
    "UTF-8",
    "-language:_"
  )
)

lazy val scalafixCommonSettings = Seq(
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  scalacOptions ++= Seq("-Wunused")
)

lazy val dockerCommonSettings = Seq(
  dockerBaseImage := "adoptopenjdk/openjdk8:x86_64-alpine-jdk8u212-b03-slim",
  dockerUpdateLatest := true,
  defaultLinuxInstallLocation in Docker := "/opt/application",
  bashScriptExtraDefines ++= Seq(
    "addJava -Xms${JVM_HEAP_MIN:-512m}",
    "addJava -Xmx${JVM_HEAP_MAX:-512m}",
    "addJava -XX:MaxMetaspaceSize=${JVM_META_MAX:-128m}",
    "addJava ${JVM_GC_OPTIONS:--XX:+UseG1GC}"
  )
)

lazy val root: Project = (project in file("."))
  .settings(
    name := "chat-alart"
  )
  .aggregate(
    chatalartServer,
    chatalartWorker
  )

