package dc10.dsl.predef.datatype

import munit.FunSuite

class SbtSuite extends FunSuite:
  
  import dc10.sbt.dsl.*
  import dc10.sbt.compiler
  import dc10.sbt.version.`1.10.5`
  import dc10.scala.dsl.{*, given}
  import dc10.scala.version.`3.3.4`
  import scala.language.implicitConversions
  
  test("base dir"):

    def `Main.scala` =
      FILE("Main.scala",
        VAL("hello", STRING, "hello, world")
      )
    val ast =
      BASEDIR("dc10-example",
        for
          s <- SRC(PACKAGE("example", `Main.scala`))
          _ <- BUILDSBT(ROOT("dc10-example", s))
        yield ()
      )

    val obtained: List[String] =
      ast.compile.virtualFile.fold(_ => Nil, l => l.filter(f => f.path.toString().contains("build.sbt")).map(f => f.contents))
      
    val expected: List[String] =
      scala.List(
        """val CatsEffectV = "3.5.2"
          |
          |ThisBuild / scalaVersion := "3.5.2"
          |ThisBuild / version := "0.1.0-SNAPSHOT"
          |
          |lazy val root = (project in file(".")).settings(
          |  name := "dc10-example",
          |  libraryDependencies ++= Seq(
          |    
          |  )
          |)""".stripMargin
      )

    assertEquals(obtained, expected)

  test("base dir crossproject"):

    def `Main.scala` =
      FILE("Main.scala",
        VAL("hello", STRING, "hello, world")
      )

    val ast =
      BASEDIR("dc10-example",
        for
          s <- SRC(PACKAGE("example", `Main.scala`))
          _ <- BUILDSBT(CROSSPROJECT("dc10-example", s))
          _ <- README("## `dc10-example`")
        yield ()
      )
    
    val obtained: List[String] =
      ast.compile.virtualFile.fold(_ => Nil, l => l.map(f => f.contents))
      
    val expected: List[String] =
      scala.List(
        """|package example
           |
           |val hello: String = "hello, world"""".stripMargin,
        """|ThisBuild / scalaVersion := "3.5.2"
           |ThisBuild / version := "0.1.0-SNAPSHOT"
           |
           |lazy val `dc10-example` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
           |  .in(file("."))
           |  .settings(
           |    name := "dc10-example",
           |    libraryDependencies ++= Seq(
           |       
           |    )
           |)""".stripMargin,
        """## `dc10-example`""".stripMargin,
        """|addSbtPlugin("org.portable-scala" %% "sbt-scalajs-crossproject" % "1.3.2")
           |
           |addSbtPlugin("org.portable-scala" %% "sbt-scala-native-crossproject" % "1.3.2")
           |
           |addSbtPlugin("org.scala-js" %% "sbt-scalajs" % "1.17.0")
           |
           |addSbtPlugin("org.scala-native" %% "sbt-scala-native" % "0.5.5")""".stripMargin
      )

    assertEquals(obtained, expected)

  