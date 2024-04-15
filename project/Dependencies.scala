import sbt._

object Dependencies {

  lazy val akkaVersion                    = "2.5.21"
  lazy val akkaHttpVersion                = "10.1.7"
  lazy val akkaPersistenceInmemoryVersion = "2.5.15.1"
  lazy val scalaTestVersion               = "3.2.17"
  lazy val levelDBVersion                 = "0.10"
  lazy val levelDbJniVersion              = "1.8"

  lazy val munit = "org.scalameta" %% "munit" % "0.7.29"
}
