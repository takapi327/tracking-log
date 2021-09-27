import sbt._

name         := "tracking-log"
organization := "io.github.takapi327"

ThisBuild / organizationName := "Takahiko Tominaga"
ThisBuild / startYear        := Some(2021)

ThisBuild / scalaVersion := "2.13.3"

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ywarn-dead-code",
  "-Ymacro-annotations"
)

libraryDependencies ++= Seq(
  guice,
  "org.uaparser" %% "uap-scala" % "0.13.0",
  /*
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.4.1",
   */
  //"ch.qos.logback" % "logback-core" % "1.3.0-alpha10",
  //"ch.qos.logback" % "logback-classic" % "1.3.0-alpha10"
  //"net.logstash.logback" % "logstash-logback-encoder" % "6.6"
)

Compile    / publishArtifact := false
packageDoc / publishArtifact := false
packageSrc / publishArtifact := false
