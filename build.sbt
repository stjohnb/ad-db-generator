ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

val circeVersion = "0.14.1"

lazy val root = (project in file(".")).settings(
  name := "ad-db-generator",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.3.11",
    "org.typelevel" %% "cats-effect-kernel" % "3.3.11",
    "org.typelevel" %% "cats-effect-std" % "3.3.11",
    "io.circe" %% "circe-core" % circeVersion,
    "commons-io" % "commons-io" % "2.6",
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
  )
)
