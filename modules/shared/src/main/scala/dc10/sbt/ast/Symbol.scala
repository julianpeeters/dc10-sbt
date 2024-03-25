package dc10.sbt.ast

sealed trait Symbol
object Symbol:

  sealed trait Build extends Symbol
  object Build:
    case class BaseDirectory(
      nme: String,
      modules: List[FileDef]
    ) extends Build

  sealed trait Project extends Symbol
  object Project:

    case class Root(
      nme: String,
      agg: List[SubProject],
      contents: List[FileDef]
    ) extends Project

    case class SubProject(
      nme: String,
      contents: List[FileDef]
    ) extends Project