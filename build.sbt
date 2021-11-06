import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "3.1.0"
ThisBuild / organization     := "uk.co.danielrendall"
ThisBuild / organizationName := "services-as-a-service"

githubOwner := "danielrendall"
githubRepository := "ServicesAsAService"
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")
releaseCrossBuild := true

lazy val root = (project in file("."))
  .settings(
    name := "services-as-a-service",
    libraryDependencies ++= Seq(
      "uk.co.danielrendall" %% "services-as-a-service-interfaces" % "0.0.1-SNAPSHOT",
      "org.nanohttpd" % "nanohttpd" % "2.3.1",
      specs2 % Test
    )
  )
