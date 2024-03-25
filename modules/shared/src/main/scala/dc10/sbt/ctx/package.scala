package dc10.sbt.ctx

import dc10.sbt.ast.{FileDef, ProjectDef}
import dc10.scala.ErrorF

extension (ctx: List[ProjectDef])
  def ext(s: ProjectDef): ErrorF[List[ProjectDef]] =
    namecheck(s).map(ctx :+ _)
  def namecheck(s: ProjectDef): ErrorF[ProjectDef] =
    // TODO
    Right(s)

extension (ctx: List[FileDef])
  def ext(s: FileDef): ErrorF[List[FileDef]] =
    namecheck(s).map(ctx :+ _)
  def namecheck(s: FileDef): ErrorF[FileDef] =
    // TODO
    Right(s)