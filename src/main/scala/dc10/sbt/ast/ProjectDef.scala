package dc10.sbt.ast

import org.tpolecat.sourcepos.SourcePos

case class ProjectDef(
  project: Symbol.Project,
  sp: SourcePos
)
