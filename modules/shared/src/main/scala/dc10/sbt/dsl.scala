package dc10.sbt

import dc10.sbt.predef.file.Files

trait dsl

object dsl extends dsl
  with Files.Mixins
  with Project.Mixins