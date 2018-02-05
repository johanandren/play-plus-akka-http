import play.core.PlayVersion

name := """play-cluster-playground"""
organization := "com.lightbend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.5.8"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.11"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.7"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.lightbend.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.lightbend.binders._"
