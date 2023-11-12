# dc10-sbt

Library for use with the `dc10-scala` code generator.
 - Library for Scala 3 (JVM only)
 - Generates code for Scala 3

```scala
"com.julianpeeters" %% "dc10-sbt" % "0.0.0"
```

### `dc10-sbt`
Use the dsl to define a basic sbt project:

```scala
import dc10.sbt.dsl.*
import dc10.scala.dsl.{*, given}
import scala.language.implicitConversions // for literals, e.g. "hello, world"

val `Main.scala` =
  FILE("Main.scala",
    VAL("hello", STRING, "hello, world")
  )
// `Main.scala`: IndexedStateT[ErrorF, List[File], List[File], ValueExpr[String, Unit]] = cats.data.IndexedStateT@6a9f8237

val snippet = 
  BASEDIR("dc10-example",
    for 
      s <- SRC(PACKAGE("example", `Main.scala`))
      _ <- BUILDSBT(ROOT("dc10-example", s))
    yield ()
  )
// snippet: IndexedStateT[ErrorF, List[FileDef], List[FileDef], Unit] = cats.data.IndexedStateT@20e85b4d
```

Use the `compiler` to render the code:

```scala
import dc10.sbt.compiler.{compile, toVirtualFile}
import dc10.sbt.version.`1.9.7`

val result: List[String] =
  snippet.compile.toVirtualFile["sbt-1.9.7"].fold(_ => Nil, l => l.map(f => f.contents))
// result: List[String] = List(
//   """package example
// 
// val hello: String = "hello, world"""",
//   """val CatsEffectV = "3.5.2"
// 
// ThisBuild / scalaVersion := "3.3.1"
// ThisBuild / version := "0.1.0-SNAPSHOT"
// 
// lazy val root = (project in file(".")).settings(
//   name := "dc10-example",
//   libraryDependencies ++= Seq(
//     "org.typelevel" %% "cats-effect" % CatsEffectV,
//   )
// )"""
// )
```