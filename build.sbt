name := """mysql-attempt"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

libraryDependencies ++= Seq(

  evolutions,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,

  "mysql" % "mysql-connector-java" % "8.0.27",

  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",

  "org.playframework.anorm" %% "anorm" % "2.6.10",

  "com.pauldijou" %% "jwt-play" % "5.0.0",
  "com.pauldijou" %% "jwt-core" % "5.0.0",
  "com.auth0" % "jwks-rsa" % "0.6.1"
)