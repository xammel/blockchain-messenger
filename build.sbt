import Dependencies._

ThisBuild / scalaVersion := "2.12.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.xammel"
ThisBuild / organizationName := "xammel"

lazy val root = (project in file("."))
  .settings(
    name := "scala-blockchain",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"        %% "akka-testkit"         % akkaVersion      % Test,
      "org.scalactic"            %% "scalactic"            % scalaTestVersion,
      "org.scalatest"            %% "scalatest"            % scalaTestVersion % "test",
      "com.typesafe.akka"        %% "akka-persistence"     % akkaVersion,
      "org.iq80.leveldb"          % "leveldb"              % levelDBVersion,
      "org.fusesource.leveldbjni" % "leveldbjni-all"       % levelDbJniVersion,
      "com.typesafe.akka"        %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"        %% "akka-actor"           % akkaVersion,
      "com.typesafe.akka"        %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"        %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster"         % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster-tools"   % akkaVersion
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
