resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.2")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"        % "0.9.16")
addSbtPlugin("io.spray"         % "sbt-revolver"        % "0.9.1")
