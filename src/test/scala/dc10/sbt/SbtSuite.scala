package dc10.dsl.predef.datatype

import munit.FunSuite

class SbtSuite extends FunSuite:
  
  import dc10.sbt.compiler
  import dc10.sbt.dsl.*
  import dc10.sbt.version.`1.9.9`
  import dc10.scala.dsl.{*, given}
  import scala.language.implicitConversions
  
  test("base dir"):

    def `Main.scala` =
      FILE("Main.scala",
        VAL("hello", STRING, "hello, world")
        // IOAPP("Main",
        //   RUN(SERVER("0.0.0.0", "8080", GET("", PRINTLN("howdy!"))))
        //   // RUN(PRINTLN("howdy!"))
        // )
      )

    val ast =
      BASEDIR("dc10-example",
        for
          s <- SRC(PACKAGE("example", `Main.scala`))
          _ <- BUILDSBT(ROOT("dc10-example", s))
        yield ()
      )
    
    val obtained: List[String] =
      ast.compile.toVirtualFile["sbt-1.9.9"].fold(_ => Nil, l => l.map(f => f.contents))
      
    val expected: List[String] =
      scala.List(
        """package example
          |
          |val hello: String = "hello, world"""".stripMargin,
        """val CatsEffectV = "3.5.2"
          |
          |ThisBuild / scalaVersion := "3.4.0"
          |ThisBuild / version := "0.1.0-SNAPSHOT"
          |
          |lazy val root = (project in file(".")).settings(
          |  name := "dc10-example",
          |  libraryDependencies ++= Seq(
          |    "org.typelevel" %% "cats-effect" % CatsEffectV,
          |  )
          |)""".stripMargin
      )

    assertEquals(obtained, expected)

  