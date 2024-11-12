package dc10.sbt

import dc10.Compiler
import dc10.scala.{Error, LibDep}

given compiler: Compiler[Statement, LibDep, Error] =
  Compiler.impl[Statement, LibDep, Error]