import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion     := "3.1.0"
ThisBuild / organization     := "uk.co.danielrendall"
ThisBuild / organizationName := "services-as-a-service"

githubOwner := "danielrendall"
githubRepository := "ServicesAsAService"
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")

lazy val root = (project in file("."))
  .settings(
    name := "services-as-a-service",
    assembly / mainClass := Some("uk.co.danielrendall.saas.server.main"),
    libraryDependencies ++= Seq(
      "uk.co.danielrendall" %% "services-as-a-service-interfaces" % "0.0.1",
      "org.nanohttpd" % "nanohttpd" % "2.3.1",
      specs2 % Test
    )
  )
