package flca.projbld.util

import java.io.File
import java.net.URLClassLoader
import java.io.InputStream
import java.io.BufferedInputStream
import scala.collection.mutable.ListBuffer
import java.io.FileInputStream

// these could moved to an utility project

object FileUtils {

  def readFile(file: File): String = {
    val source = scala.io.Source.fromFile(file)
    val result = source.mkString
    source.close
    result
  }

  def getClasspathBinDir(): File = {
    val cl = ClassLoader.getSystemClassLoader();
    val urls = cl.asInstanceOf[URLClassLoader].getURLs();
    new File(urls(0).getFile)
  }

  def isBinary(file: File): Boolean = isBinary(new FileInputStream(file))

  def isBinary(is: InputStream): Boolean = {
    val bis = new BufferedInputStream(is)
    var c = bis.read()
    while (c > -1) {
      if (c < 9 || c > 127 || (c > 14 && c < 32)) return true
      c = bis.read()
    }
    false
  }

  /**
   * recursively find all files from a given root folder
   */
  def findAllFiles(f: File): List[File] = {
    f :: (if (f.isDirectory) f.listFiles().toList.flatMap(findAllFiles) else Nil)
  }

  //   same using Stream
  def findAllFiles2(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(findAllFiles2) else Stream.empty)

  // same using mutable ListBuffer
  //  def findAllFiles3(f: File): List[File] = {
  //    var r = ListBuffer[File]()
  //    def iter(f:File): Unit = {
  //      r += f
  //	  if (f.isDirectory) f.listFiles().toList.foreach(iter) 
  //    }
  //    iter(f)
  //    r.toList
  //  }

  /**
   * find all resources (file(s) in the current classpath, that match the given function
   */
  def findResourceFiles(fun: File => Boolean): List[File] = {
    val allfiles = findAllFiles(getClasspathBinDir)
    allfiles filter ((f) => fun(f))
  }

  /**
   * find all file(s) from the given rootdir, that match the given function
   */
  def findFiles(rootdir: File, fun: File => Boolean): List[File] = {
    val allfiles = findAllFiles(rootdir)
    allfiles filter ((f) => fun(f))
  }

  def separator() = {
    System.getProperty("file.separator");
  }

}