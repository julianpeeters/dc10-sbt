package dc10.sbt

import dc10.File
import dc10.scala.{LibDep, Statement}
import java.nio.file.Path

sealed trait Symbol
object Symbol:

  sealed trait Build
  object Build:

    case class SourceDir(
      path: Path,
      deps: Set[LibDep],
      files: List[File[Statement]]
    ) extends Build

    sealed trait Extras extends Symbol
    object Extras:
      case class Gitignore() extends Extras
      case class License() extends Extras
      case class Readme(text: String) extends Extras

  sealed trait Project extends Symbol
  object Project:

    case class AddSbtPlugin(
      libDep: LibDep
    ) extends Project

    case class CrossProject(
      nme: String,
      src: Build.SourceDir,
    ) extends Project

    case class Root(
      nme: String,
      agg: List[SubProject],
      src: Build.SourceDir,
    ) extends Project

    case class SubProject(
      nme: String,
      src: Build.SourceDir,
    ) extends Project