package dc10.sbt

import cats.data.StateT
import cats.syntax.all.given
import dc10.File
import dc10.sbt.compiler
import dc10.sbt.Statement.ProjectDef
import dc10.sbt.Symbol.Build.SourceDir
import dc10.scala.{ErrorF, LibDep}

trait Project[F[_], G[_]]:
  def CROSSPROJECT[A](nme: String, src: SourceDir): F[Symbol.Project]
  def PROJECT[A](nme: String, src: SourceDir): F[Symbol.Project]
  def ROOT[A](nme: String, src: SourceDir): F[Unit]
    
object Project:

  val scalaJsCross: LibDep     = LibDep("org.portable-scala", "sbt-scalajs-crossproject",      "1.3.2")
  val scalaNativeCross: LibDep = LibDep("org.portable-scala", "sbt-scala-native-crossproject", "1.3.2")
  val scalaJs: LibDep          = LibDep("org.scala-js",       "sbt-scalajs",                   "1.17.0")
  val scalaNative: LibDep      = LibDep("org.scala-native",   "sbt-scala-native",              "0.5.5")

  trait Mixins extends Project[
    StateT[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), _],
    StateT[ErrorF, (Set[LibDep], List[File[dc10.scala.Statement]]), _],
  ]:
    
    def CROSSPROJECT[A](
      nme: String,
      src: SourceDir
    ): StateT[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), Symbol.Project] =
      for
        p <- StateT.pure(Symbol.Project.CrossProject(nme, src))
        d <- StateT.pure[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), ProjectDef](ProjectDef(p))
        _ <- List(scalaJsCross, scalaNativeCross, scalaJs, scalaNative).toList.traverse(l =>
          StateT.modifyF[ErrorF, (Set[LibDep], List[dc10.sbt.Statement])](ctx => ctx.dep(l)))
        _ <- StateT.modifyF[ErrorF, (Set[LibDep], List[dc10.sbt.Statement])](ctx => ctx.ext(d))
      yield p
      
    def PROJECT[A](
      nme: String,
      src: SourceDir
    ): StateT[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), Symbol.Project] =
      for
        p <- StateT.pure(Symbol.Project.SubProject(nme, src))
        d <- StateT.pure[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), ProjectDef](ProjectDef(p))
        _ <- src.deps.toList.traverse(l => StateT.modifyF[ErrorF, (Set[LibDep], List[dc10.sbt.Statement])](ctx => ctx.dep(l)))
        _ <- StateT.modifyF[ErrorF, (Set[LibDep], List[dc10.sbt.Statement])](ctx => ctx.ext(d))
      yield p
      
    def ROOT[A](
      nme: String,
      src: SourceDir
    ): StateT[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), Unit] =
      for
        d <- StateT.pure[ErrorF, (Set[LibDep], List[dc10.sbt.Statement]), ProjectDef](ProjectDef(Symbol.Project.Root(nme, Nil, src)))
        _ <- StateT.modifyF[ErrorF, (Set[LibDep], List[dc10.sbt.Statement])](ctx => ctx.ext(d))
      yield ()