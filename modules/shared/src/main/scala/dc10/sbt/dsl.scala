package dc10.sbt

import dc10.sbt.predef.{Build, Project}

trait dsl

object dsl extends dsl
  with Build.Mixins
  with Project.Mixins