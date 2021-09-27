import sbt._

name         := "tracking-log"
organization := "io.github.takapi327"

ThisBuild / organizationName := "Takahiko Tominaga"
ThisBuild / startYear        := Some(2021)

ThisBuild / scalaVersion := "2.13.3"

/**
 * Build mode
 */
import scala.sys.process._
val branch  = ("git branch".lineStream_!).find(_.head == '*').map(_.drop(2)).getOrElse("")
val release = (branch == "master" || branch.startsWith("release"))

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
  "org.uaparser" %% "uap-scala" % "0.13.0"
  /*
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.4.1",
   */
  //"ch.qos.logback" % "logback-core" % "1.3.0-alpha10",
  //"ch.qos.logback" % "logback-classic" % "1.3.0-alpha10"
  //"net.logstash.logback" % "logstash-logback-encoder" % "6.6"
)

/** Setting sbt-release */
import ReleaseTransformations._
releaseVersionBump   := sbtrelease.Version.Bump.Bugfix
releaseTagComment    := s"Releasing ${(ThisBuild / version).value}[ci skip]"
releaseCommitMessage := s"Setting version to ${(ThisBuild / version).value}[ci skip]"

publishTo := {
  val path = if (release) "releases" else "snapshots"
  Some("Takapi snapshots" at "s3:://maven.takapi.net.s3-ap-northeast-1.amazonaws.com/" + path)
}
Compile / publishArtifact := false

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
