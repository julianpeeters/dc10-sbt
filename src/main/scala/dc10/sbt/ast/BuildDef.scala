package dc10.sbt.ast

import org.tpolecat.sourcepos.SourcePos

case class BuildDef(
  build: Symbol.Build,
  sp: SourcePos
)
