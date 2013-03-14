import sbt._
import sbt.Keys._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  val appName         = "paypal"
  val appVersion      = "0.1.0-SNAPSHOT"
  val appDependencies = Seq(
    "com.typesafe"   %  "config"    % "1.0.0",
    "org.specs2"     %% "specs2"    % "1.14" //% "test"
  )

  crossScalaVersions := Seq("2.10.0")
  scalaVersion := "2.10.0"

  scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.6", "-unchecked",
    "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    organization := "com.micronautics",
    resolvers += Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
    publishMavenStyle := false,
    publishTo <<= (version) { version: String =>
       val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
       val (name, url) = if (version.contains("-SNAPSHOT"))
         ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
       else
         ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
       Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
    }
  )

  logLevel := Level.Error
  logLevel in compile := Level.Warn
}
