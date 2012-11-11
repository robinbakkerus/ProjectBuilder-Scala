package flca.projbld

import flca.projbld.util.EnumerationIterator
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.Channels
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import scala.io.Source
import com.codahale.jerkson.Json.parse
import java.io.FileInputStream
import java.io.InputStream
import flca.projbld.util.FileUtils

/**
 * this is the obect that will actually copy the source dir to the target location,
 * and make the nessecary from-to substitutions, on the filename and inside the ascii files if needed
 */
class ProjectBuilder(source: File, fromtos: Map[String, String], target: File) extends Constants {

  /**
   * The public entry point
   * @param source the template directory of zip file, that will be copied and substituted
   * @param fromtos a Map of from -> to substitutions (case-sensistive)
   * @param target target directory
   */
  def execute() {
    if (source.isDirectory()) processFolder(source, fromtos, target, processFile)
    else processZipfile(source, fromtos, target, processZipentry)
  }

  private def targetDir(targetRoot: File, sourceName: String, fromtos: Map[String, String]): File = {
    val dir = new File(targetRoot.getAbsoluteFile() + "/" + sourceName)
    val newdir = substituteFilname(dir, fromtos)
    newdir.mkdirs()
    newdir
  }

  private def targetFile(targetRoot: File, sourceName: String, fromtos: Map[String, String]): File = {
    val file = new File(targetRoot.getAbsoluteFile() + "/" + sourceName)
    substituteFilname(file, fromtos)
  }

  private def processZipfile(source: File, fromtos: Map[String, String], target: File,
    aProcessor: (ZipFile, ZipEntry, File, Map[String, String]) => Unit): Unit = {
    val zipfile = new ZipFile(source);
    val iter = new EnumerationIterator(zipfile.entries())
    iter.foreach(entry => aProcessor(zipfile, entry, target, fromtos))
  }

  private def processFolder(source: File, fromtos: Map[String, String], target: File,
    aProcessor: (File, File, Map[String, String]) => Unit): Unit = {
    val allfiles = FileUtils.findAllFiles(source)
    allfiles.foreach(f => aProcessor(f, target, fromtos))
  }

  private def processZipentry(zf: ZipFile, ze: ZipEntry, targetRoot: File, fromtos: Map[String, String]): Unit = {
    doProcess(zf.getInputStream(ze), ze.getName, getFileType(zf, ze), targetRoot, fromtos)
  }

  private def processFile(sourceFile: File, targetRoot: File, fromtos: Map[String, String]): Unit = {
    val fis = if (!sourceFile.isDirectory()) new FileInputStream(sourceFile) else null
    doProcess(fis, sourcePath(sourceFile), getFileType(sourceFile), targetRoot, fromtos)
  }

  lazy val sourcePathLength = getSourcePathLength()

  private def getSourcePathLength(): Int = {
    source.getParent().length() + 1
  }

  private def sourcePath(sourceFile: File): String = {
    sourceFile.getPath().substring(sourcePathLength)
  }

  private def doProcess(is: InputStream, sourcePath: String, filetyp: FileType, targetRoot: File, fromtos: Map[String, String]): Unit = {
    val outfile = targetFile(targetRoot, sourcePath, fromtos)
    filetyp match {
      case x: Directory => {
        val targetdir = targetDir(targetRoot, sourcePath, fromtos)
        println("created directory " + targetdir)
      }
      case x: Binary => {
        copyEntry(is, outfile)
        println("binary copy " + sourcePath)
      }
      case _ => {
        copyAndSubstitute(is, outfile, fromtos)
        println("ascii copy " + sourcePath)
      }
    }
  }

  private def copyEntry(is: InputStream, target: File) = {
    new FileOutputStream(target) getChannel () transferFrom (
      Channels.newChannel(is), 0, Long.MaxValue)
  }

  private def copyAndSubstitute(is: InputStream, target: File, fromtos: Map[String, String]) = {
    val outlines = Source.fromInputStream(is).getLines.toList.map(s => subsLine(s, fromtos))
    writeOutlines(outlines, target)
  }

  private def subsLine(line: String, fromtos: Map[String, String]): String = {
    substitute(line, fromtos)
  }

  private def substituteFilname(file: File, fromtos: Map[String, String]): File = {
    // first the regular names
    val fname = substitute(file.getPath(), fromtos)
    // end now the path
    fromtos.foldLeft(fname)((acc, kv) => substPath(acc, kv._1, kv._2))
    new File(fname)
  }

  private def substPath(path: String, fromStr: String, toStr: String): String = {
    if (fromStr.indexOf(".") > 0) {
      val fromStrPath = fromStr.replaceAll("\\.", "/")
      val toStrPath = toStr.replaceAll("\\.", "/")
      path.replaceAll(fromStrPath, toStrPath)
    } else {
      path
    }
  }

  private def substitute(line: String, fromtos: Map[String, String]): String = {
    fromtos.foldLeft(line)((acc, kv) => acc.replaceAll(kv._1, kv._2))
  }

  private def writeOutlines(outlines: List[String], target: File) = {
    val fos = new FileOutputStream(target);
    outlines.foreach(s => fos.write((s + "\n").getBytes()))
    fos.close();
  }

  abstract class FileType
  case class Directory extends FileType
  case class Binary extends FileType
  case class Ascii extends FileType

  def getFileType(zf: ZipFile, ze: ZipEntry): FileType = {
    if (ze.isDirectory()) new Directory
    else if (FileUtils.isBinary(zf.getInputStream(ze))) new Binary
    else new Ascii
  }

  def getFileType(file: File): FileType = {
    if (file.isDirectory()) new Directory
    else if (FileUtils.isBinary(new FileInputStream(file))) new Binary
    else new Ascii
  }

}