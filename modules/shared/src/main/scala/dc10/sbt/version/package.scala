package dc10.sbt.version

import dc10.compile.Renderer
import dc10.sbt.ast.{ProjectDef, Symbol}
import dc10.scala.Error

given `1.9.9`: Renderer["sbt-1.9.9", Error, List[ProjectDef]] =
  new Renderer["sbt-1.9.9", Error, List[ProjectDef]]:

    override def render(input: List[ProjectDef]): String =
      """val CatsEffectV = "3.5.2"
        |
        |ThisBuild / scalaVersion := "3.4.0"
        |ThisBuild / version := "0.1.0-SNAPSHOT"
        |
        |""".stripMargin ++
        input.map(stmt => stmt match
          case ProjectDef(p, sp) => p match
            case Symbol.Project.Root(nme, agg, contents) =>
              s"""lazy val root = (project in file(".")).settings(
                 |  name := "${nme}",
                 |  libraryDependencies ++= Seq(
                 |    "org.typelevel" %% "cats-effect" % CatsEffectV,
                 |  )
                 |)""".stripMargin
            case Symbol.Project.SubProject(nme, contents) => nme
        ).mkString("\n\n")
   
    override def renderErrors(errors: List[Error]): String =
      errors.map(_.toString()).mkString("\n")

    override def version: "sbt-1.9.9" =
      "sbt-1.9.9"
