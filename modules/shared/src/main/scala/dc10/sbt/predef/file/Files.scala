package dc10.sbt.predef.file

import cats.data.StateT
import cats.syntax.all.given
import dc10.File
import dc10.sbt.compiler
import dc10.sbt.Statement
import dc10.sbt.Symbol.Build.SourceDir
import dc10.sbt.Symbol.Build.Extras.{Gitignore, License, Readme}
import dc10.sbt.Symbol.Project.{AddSbtPlugin, CrossProject, SubProject, Root}
import dc10.scala.{ErrorF, LibDep}
import java.nio.file.Path

trait Files[F[_], G[_], H[_]]:
  def BASEDIR[A](nme: String, files: F[A]): F[Unit]
  def BUILDSBT[A](statements: G[A]): F[A]
  def GITIGNORE: F[Unit]
  def LICENSE: F[Unit]
  def README(text: String): F[Unit]
  def SRC[A](packages: H[A]): F[SourceDir]
  given refF: Conversion[File[dc10.sbt.Statement], F[File[dc10.sbt.Statement]]]

object Files:

  trait Mixins extends Files[
    StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), _],
    StateT[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), _],
    StateT[ErrorF, (Set[LibDep], List[File[dc10.scala.Statement]]), _]
  ]:

    def BASEDIR[A](
      nme: String,
      files: StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), A]
    ): StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), Unit] =
      for
        ((ds, ms)) <- StateT.liftF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), ((Set[LibDep], List[File[dc10.sbt.Statement]]))](files.runEmptyS)
        d = ds.map(d => dc10.sbt.Statement.ProjectDef(AddSbtPlugin(d)))
        f <- StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), File[dc10.sbt.Statement]](File(Path.of(s"project/plugins.sbt"), d.toList).addParent(Path.of(nme)))
        c <- StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), List[File[dc10.sbt.Statement]]]((ms).map(f => f.addParent(Path.of(nme))):+f)
        _ <- c.traverse(f => StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(f)))
      yield ()

    def BUILDSBT[A](
      statements: StateT[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), A]
    ): StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), A] =
      for
        ((ds, ms), a) <- StateT.liftF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), ((Set[LibDep], List[dc10.sbt.Statement]), A)](statements.runEmpty)
        _ <- ms.flatTraverse(m => m match
          case Statement.ProjectDef(project) => project match
            case AddSbtPlugin(libDep) => StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), List[Unit]]((List()))  
            case CrossProject(nme, src) => src.files.traverse(p => StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(File(Path.of("shared", "src", "main", "scala").resolve(p.path), p.contents.map(dc10.sbt.Statement.ScalaStatement.apply)))))
            case Root(nme, agg, src) => src.files.traverse(p => StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(File(Path.of("src", "main", "scala").resolve(p.path), p.contents.map(dc10.sbt.Statement.ScalaStatement.apply)))))
            case SubProject(nme, src) =>src.files.traverse(p => StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(File(Path.of("src", "main", "scala").resolve(p.path), p.contents.map(dc10.sbt.Statement.ScalaStatement.apply)))))
          case Statement.LicenseStatement(s) => StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), List[Unit]]((List()))
          case Statement.GitignoreStatement(s) => StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), List[Unit]]((List()))
          case Statement.ReadmeStatement(s) => StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), List[Unit]]((List()))
          case Statement.ScalaStatement(statement) => StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), List[Unit]]((List()))
        )
        d <- StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), File[dc10.sbt.Statement]](File(Path.of("build.sbt"), ms))
        _ <- ds.toList.traverse(l => StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.dep(l)))
        _ <- StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(d))
      yield a

    def GITIGNORE: StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), Unit] =
      for
        d <- StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), File[dc10.sbt.Statement]](File(Path.of(".gitignore"), List(Statement.GitignoreStatement(Gitignore()))))
        _ <- StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(d))
      yield()

    def LICENSE: StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), Unit] =
      for
        f <- StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), File[dc10.sbt.Statement]](File(Path.of("LICENSE"), List(Statement.LicenseStatement(License()))))
        _ <- StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(f))
      yield ()
          
    def README(text: String): StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), Unit] =
      for
        f <- StateT.pure[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), File[dc10.sbt.Statement]](File(Path.of("README.md"), List(Statement.ReadmeStatement(Readme(text)))))
        _ <- StateT.modifyF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]])](ctx => ctx.ext(f))
      yield ()

    def SRC[A](
      packages: StateT[ErrorF, (Set[LibDep], List[File[dc10.scala.Statement]]), A]
    ): StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), SourceDir] =
      for
        ((ds, s), a) <- StateT.liftF[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), ((Set[LibDep], List[File[dc10.scala.Statement]]), A)](packages.runEmpty)
      yield SourceDir(Path.of("."), ds, s.map(f => f.copy(path = Path.of("src", "main", "scala").resolve(f.path))))

    given refF: Conversion[File[dc10.sbt.Statement], StateT[ErrorF, (Set[LibDep], List[File[dc10.sbt.Statement]]), File[dc10.sbt.Statement]]] =
      v => StateT.pure(v)