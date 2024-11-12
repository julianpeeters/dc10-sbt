package dc10.sbt

import dc10.sbt.Symbol.Project
import dc10.sbt.Symbol.Build.Extras.{Gitignore, License, Readme}

sealed trait Statement
object Statement:
  case class ProjectDef(project: Project) extends Statement
  case class LicenseStatement(license: License) extends Statement
  case class GitignoreStatement(gitignore: Gitignore) extends Statement
  case class ReadmeStatement(readme: Readme) extends Statement
  case class ScalaStatement(statement: dc10.scala.Statement) extends Statement
