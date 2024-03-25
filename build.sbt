val CatsV = "2.10.0"
val Dc10ScalaV = "0.7.1"
val Fs2V = "3.7.0"
val MUnitV = "0.7.29"
val SourcePosV = "1.1.0"

inThisBuild(List(
  crossScalaVersions := Seq(scalaVersion.value),
  description := "Library for use with the `dc10-scala` code generator",
  organization := "com.julianpeeters",
  homepage := Some(url("https://github.com/julianpeeters/dc10-sbt")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "julianpeeters",
      "Julian Peeters",
      "julianpeeters@gmail.com",
      url("http://github.com/julianpeeters")
    )
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Werror",
    "-source:future",
    "-Wunused:all",
    "-Wvalue-discard"
  ),
  scalaVersion := "3.4.0",
  versionScheme := Some("semver-spec"),
))

lazy val `dc10-sbt` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
   .in(file("modules"))
  .settings(
    name := "dc10-sbt",
    libraryDependencies ++= Seq(
      "com.julianpeeters" %%% "dc10-scala" % Dc10ScalaV,
      "org.tpolecat"      %%% "sourcepos"  % SourcePosV,
      "org.typelevel"     %%% "cats-core"  % CatsV,
      "org.scalameta"      %% "munit"      % MUnitV      % Test
    )
  )
  .jsSettings(test := {})
  .nativeSettings(test := {})

lazy val docs = project.in(file("docs/gitignored"))
  .settings(
    mdocOut := file("."),
    mdocVariables := Map(
      "SCALA" -> crossScalaVersions.value.map(e => e.reverse.dropWhile(_ != '.').drop(1).reverse + "+").mkString(", "),
      "VERSION" -> version.value.takeWhile(_ != '+'),
    )
  )
  .dependsOn(`dc10-sbt`.jvm)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)