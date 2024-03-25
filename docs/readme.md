# dc10-sbt

Library for use with the `dc10-scala` code generator.
 - Library for Scala @SCALA@ (JS, JVM, and Native platforms)
 - Generates code for Scala @SCALA@

```scala
"com.julianpeeters" %% "dc10-sbt" % "@VERSION@"
```

### `dc10-sbt`
Use the dsl to define a basic sbt project:

```scala mdoc:reset
import dc10.sbt.dsl.*
import dc10.scala.dsl.{*, given}
import scala.language.implicitConversions // for literals, e.g. "hello, world"

val `Main.scala` =
  FILE("Main.scala",
    VAL("hello", STRING, "hello, world")
  )

val snippet = 
  BASEDIR("dc10-example",
    for 
      s <- SRC(PACKAGE("example", `Main.scala`))
      _ <- BUILDSBT(ROOT("dc10-example", s))
    yield ()
  )
```

Use the `compiler` to render the code:

```scala mdoc
import dc10.sbt.compiler.{compile, toVirtualFile}
import dc10.sbt.version.`1.9.9`

val result: List[String] =
  snippet.compile.toVirtualFile["sbt-1.9.9"].fold(_ => Nil, l => l.map(f => f.contents))
```