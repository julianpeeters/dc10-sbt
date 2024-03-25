package dc10.sbt.ast

import dc10.scala.File
import java.nio.file.Path

sealed trait FileDef
object FileDef:
  
  case class License(path: Path, contents: String) extends FileDef

  case class ReadMe(path: Path, contents: String) extends FileDef

  case class SbtFile(path: Path, contents: List[ProjectDef]) extends FileDef

  case class SourceDir(contents: List[File]) extends FileDef
  object SourceDir:
    def apply(contents: List[File]): SourceDir =
      new SourceDir(contents.map(c => c.addParent(Path.of("src", "main", "scala"))))

  extension (file: FileDef)
    def addParent(path: Path): FileDef =
      file match
        case f@License(p, _) => f.copy(path = path.resolve(p))
        case f@ReadMe(p, _) => f.copy(path = path.resolve(p))
        case f@SbtFile(p, _) => f.copy(path = path.resolve(p))
        case f@SourceDir(c) => f.copy(contents = c.map(f => f.copy(path = path.resolve(f.path))))
      

