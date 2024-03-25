package dc10.sbt

import cats.data.StateT
import dc10.compile.{Compiler, Renderer, VirtualFile}
import dc10.sbt.ast.{FileDef, ProjectDef}
import dc10.scala.{Error, ErrorF, Statement}
import dc10.scala.version.`3.4.0`

implicit object compiler extends Compiler[
  ErrorF,
  List,
  Error,
  ProjectDef,
  FileDef
]:

  type Ctx[F[_], L, A] = StateT[F, L, A]

  extension [C, D] (ast: StateT[ErrorF, List[D], C])
    def compile: ErrorF[List[D]] =
      ast.runEmptyS

  extension (res: ErrorF[List[ProjectDef]])
    def toString[V](
      using R: Renderer[V, Error, List[ProjectDef]]
    ): String =
      res.fold(R.renderErrors, R.render)

  extension (res: ErrorF[List[ProjectDef]])
    def toStringOrError[V](
      using R: Renderer[V, Error, List[ProjectDef]]
    ): ErrorF[String] =
      res.map(R.render)

  extension (res: ErrorF[List[FileDef]])
    def toVirtualFile[V](
      using R: Renderer[V, Error, List[ProjectDef]]
    ): ErrorF[List[VirtualFile]] =
      for
        fds <- res
      yield fds.flatMap(f =>
        f match
          case FileDef.License(path, contents) =>
            List(VirtualFile(path, contents))
          case FileDef.ReadMe(path, contents) =>
            List(VirtualFile(path, contents))
          case FileDef.SbtFile(path, contents) =>
            List(VirtualFile(path, R.render(contents)))
          case FileDef.SourceDir(contents) =>
            val S: Renderer["scala-3.4.0", Error, List[Statement]] =
              summon[Renderer["scala-3.4.0", Error, List[Statement]]]
            contents.map(c => VirtualFile(c.path, S.render(c.contents)))
      )