package dc10.sbt.predef

import cats.data.StateT
import dc10.sbt.ast.{FileDef, ProjectDef, Symbol}
import dc10.sbt.ctx.ext
import dc10.scala.ErrorF
import org.tpolecat.sourcepos.SourcePos

trait Project[F[_], G[_]]:
  def PROJECT[A](nme: String, packages: G[A])(using sp: SourcePos): F[A]
  def ROOT[A](nme: String, source: FileDef)(using sp: SourcePos): F[Unit]
    
object Project:

  trait Mixins extends Project[
    [A] =>> StateT[ErrorF, List[ProjectDef], A],
    [A] =>> StateT[ErrorF, List[FileDef], A]
  ]:
    
    def PROJECT[A](
      nme: String,
      packages: StateT[ErrorF, List[FileDef], A]
    )(using sp: SourcePos): StateT[ErrorF, List[ProjectDef], A] =
      for
        (ms, a) <- StateT.liftF[ErrorF, List[ProjectDef], (List[FileDef], A)](packages.runEmpty)
        d <- StateT.pure[ErrorF, List[ProjectDef], ProjectDef](
          ProjectDef(Symbol.Project.SubProject(nme, ms), sp))
        _ <- StateT.modifyF[ErrorF, List[ProjectDef]](ctx => ctx.ext(d))
      yield a
      
    def ROOT[A](nme: String, source: FileDef)(using sp: SourcePos): StateT[ErrorF, List[ProjectDef], Unit] =
      for
        s <- StateT.pure[ErrorF, List[ProjectDef], FileDef](source)
        d <- StateT.pure[ErrorF, List[ProjectDef], ProjectDef](
          ProjectDef(Symbol.Project.Root(nme, Nil, List(s)), sp))
        _ <- StateT.modifyF[ErrorF, List[ProjectDef]](ctx => ctx.ext(d))
      yield ()