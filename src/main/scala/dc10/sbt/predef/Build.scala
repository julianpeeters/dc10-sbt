package dc10.sbt.predef

import cats.data.StateT
import cats.syntax.all.toTraverseOps
import dc10.sbt.ast.BuildDef
import dc10.sbt.ast.FileDef
import dc10.sbt.ast.Symbol.Build.BaseDirectory
import dc10.sbt.ast.ProjectDef
import dc10.sbt.ctx.ext
import dc10.scala.File
import dc10.scala.ErrorF
import java.nio.file.Path
import org.tpolecat.sourcepos.SourcePos

trait Build[F[_], G[_], H[_]]:
  def BASEDIR[A](nme: String, files: F[A])(using sp: SourcePos): F[A]
  def BUILDSBT[A](projects: G[A])(using sp: SourcePos): F[A]
  def SRC[A](packages: H[A])(using sp: SourcePos): F[FileDef]
  
object Build:

  trait Mixins extends Build[
    [A] =>> StateT[ErrorF, List[FileDef], A],
    [A] =>> StateT[ErrorF, List[ProjectDef], A],
    [A] =>> StateT[ErrorF, List[File], A],
  ]:
    def BASEDIR[A](
      nme: String,
      files: StateT[ErrorF, List[FileDef], A]
    )(using sp: SourcePos): StateT[ErrorF, List[FileDef], A] =
      for
        (ms, a) <- StateT.liftF[ErrorF, List[FileDef], (List[FileDef], A)](files.runEmpty)
        c <- StateT.pure(ms.map(f => f.addParent(Path.of(nme))))
        _ <- StateT.pure[ErrorF, List[FileDef], BuildDef](BuildDef(BaseDirectory(nme, c), sp))
        _ <- c.traverse(f => StateT.modifyF[ErrorF, List[FileDef]](ctx => ctx.ext(f)))
      yield a

    def BUILDSBT[A](projects: StateT[ErrorF, List[ProjectDef], A])(using sp: SourcePos): StateT[ErrorF, List[FileDef], A] =
      for
        (ms, a) <- StateT.liftF[ErrorF, List[FileDef], (List[ProjectDef], A)](projects.runEmpty)
        f <- StateT.pure[ErrorF, List[FileDef], FileDef](FileDef.SbtFile(Path.of(s"build.sbt"), ms))
        _ <- StateT.modifyF[ErrorF, List[FileDef]](ctx => ctx.ext(f))
      yield a
  
    def SRC[A](packages: StateT[ErrorF, List[File], A])(using sp: SourcePos): StateT[ErrorF, List[FileDef], FileDef] =
      for
        s <- StateT.liftF[ErrorF, List[FileDef], List[File]](packages.runEmptyS)
        f <- StateT.pure[ErrorF, List[FileDef], FileDef](FileDef.SourceDir(s))
        _ <- StateT.modifyF[ErrorF, List[FileDef]](ctx => ctx.ext(f))
      yield f
  