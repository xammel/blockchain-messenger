import Dependencies._

ThisBuild / scalaVersion := "2.12.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.xammel"
ThisBuild / organizationName := "xammel"

//resolvers ++= Seq(
//  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
//  "dnvriend" at "http://dl.bintray.com/dnvriend/maven",
//  Resolver.jcenterRepo
//)

lazy val akkaVersion                    = "2.5.21"
lazy val akkaHttpVersion                = "10.1.7"
lazy val akkaPersistenceInmemoryVersion = "2.5.15.1"
lazy val scalaTestVersion               = "3.0.5"
lazy val circeVersion                   = "0.14.3"

lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val root = (project in file("."))
  .settings(
    name := "scala-blockchain",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"        %% "akka-testkit"         % akkaVersion % Test,
      "org.scalactic"            %% "scalactic"            % "3.2.17",
      "org.scalatest"            %% "scalatest"            % "3.2.17"    % "test",
      "com.typesafe.akka"        %% "akka-persistence"     % akkaVersion,
      "org.iq80.leveldb"          % "leveldb"              % "0.10",
      "org.fusesource.leveldbjni" % "leveldbjni-all"       % "1.8",
      "com.typesafe.akka"        %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"        %% "akka-actor"           % akkaVersion,
      "com.typesafe.akka"        %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"        %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster"         % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster-tools"   % akkaVersion,
    ) ++ circeDependencies
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
