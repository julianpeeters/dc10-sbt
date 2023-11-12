val CatsV = "2.9.0"
val Dc10ScalaV = "0.6.0"
val Fs2V = "3.7.0"
val MUnitV = "0.7.29"
val SourcePosV = "1.1.0"

ThisBuild / description := "sbt project generation"
ThisBuild / organization := "com.julianpeeters"
ThisBuild / scalaVersion := "3.4.0-RC1-bin-20231025-8046a8b-NIGHTLY"
ThisBuild / versionScheme := Some("semver-spec")

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Werror",
    "-source:future",
    "-Wunused:all",
    "-Wvalue-discard"
  ),
  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % MUnitV % Test
  )
)

lazy val `dc10-sbt` = (project in file("."))
  .settings(
    commonSettings,
    name := "dc10-sbt",
    libraryDependencies ++= Seq(
      "com.julianpeeters" %% "dc10-scala" % Dc10ScalaV,
      "org.tpolecat"      %% "sourcepos"  % SourcePosV,
      "org.typelevel"     %% "cats-core"  % CatsV,
    )
  )

lazy val docs = project.in(file("docs/gitignored"))
  .settings(
    mdocOut := `dc10-sbt`.base,
    mdocVariables := Map(
      "SCALA" -> crossScalaVersions.value.map(e => e.takeWhile(_ != '.')).mkString(", "),
      "VERSION" -> version.value.takeWhile(_ != '+'),
    )
  )
  .dependsOn(`dc10-sbt`)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)