ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val AkkaVersion = "2.6.19"
val AkkaHttpVersion = "10.2.9"
val LiftJsonVersion = "3.5.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
  "com.typesafe.akka" %% "akka-http"      % AkkaHttpVersion,
  "net.liftweb"       %% "lift-json"        % LiftJsonVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "WebsocketExample"
  )
