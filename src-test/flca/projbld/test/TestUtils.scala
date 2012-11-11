package flca.projbld.test

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import flca.projbld.util.FileUtils
import java.io.File
import java.util.Date
import flca.projbld.ProjBuilderMain
import flca.projbld.Constants

@RunWith(classOf[JUnitRunner])
class TestUtils extends FunSuite with Constants {

  val TEST_DIR = "/my-dev/dbs"

  test("find all files") {
    val starttime = new Date().getTime
    val files = FileUtils.findAllFiles(new File(TEST_DIR))
    val endtime = new Date().getTime
    println("found " + files.size + " files in " + (endtime - starttime) + " msecs")
    assert(files.size > 100, "er moeten een flink aantal gevonden worden!")
  }

  test("find all files using Stream's") {
    val starttime = new Date().getTime
    val files = FileUtils.findAllFiles2(new File(TEST_DIR))
    assert(files.size > 100, "er moeten een flink aantal gevonden worden!")
    val endtime = new Date().getTime
    println("found " + files.size + " files in " + (endtime - starttime) + " msecs")
  }
  //
  //  ignore("find all files using Mutable ListBuffer") {
  //    val starttime = new Date().getTime
  //    val files = FileUtils.findAllFiles3(new File("c:/Temp"))
  //    assert(files.size > 100, "er moeten een flink aantal gevonden worden!" )
  //    val endtime = new Date().getTime
  //    println("found " + files.size + " files in " + (endtime - starttime) + " msecs")
  //    files.foreach(println)
  //  }

  test("get the current bin dir") {
    val bindir = FileUtils.getClasspathBinDir
    assert(bindir != null && bindir.getPath().contains("bin"))
    println(bindir.getPath)
  }

  test("find templates") {
    if (System.getProperty(TEMPLATES_DIR) != null) {
      val templates = ProjBuilderMain.findTemplateDescriptors
      assert(templates.size > 0)
    } else {
      println("set -Dtemplates.dir=xxx to test this method")
    }
  }

  test("find resource ") {
    val foundfiles = FileUtils.findResourceFiles(f => { f.getName().endsWith(".jpg") })
    assert(foundfiles.size > 0, "er moeten een jpeg gevonden worden!")
    foundfiles.foreach(println)
  }

  test("test isBinary ") {
    val jpegs = FileUtils.findResourceFiles(f => { f.getName().endsWith(".jpg") })
    assert(FileUtils.isBinary(jpegs(0)))
    val asciifiles = FileUtils.findResourceFiles(f => { f.getName().endsWith("._java") })
    assert(!FileUtils.isBinary(asciifiles(0)))
  }

  //  test("find source dir") {
  //    val dir = FileUtils.findResourceFiles(file => file.getName.equals("TemplateWizzard"))
  //    println(dir(0))
  //    println("--------------------")
  //    val files = FileUtils.findAllFiles(dir(0))
  //    files.foreach(println)
  //  }
}