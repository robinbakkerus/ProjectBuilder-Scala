package flca.projbld

import java.io.File
import java.io.FileNotFoundException
import java.net.URLClassLoader
import scala.collection.mutable.ListBuffer
import com.codahale.jerkson.Json.parse
import flca.projbld.util.FileUtils
import flca.projbld.gui.TemplateGui

/**
 * this is main Scala class that controls the entire application
 */

object ProjBuilderMain extends Constants {

  def main(args: Array[String]) = {
    // find all template info files, and deserialize thes json file(s) to TemplateData objects 
    val templates = findAllTemplates
    // show the Swt gui where the select TemplateData must be filled in by user
    val data = TemplateGui.show(templates)
    // and with filled data generate the target project(s)
    if (data != null) new ProjectBuilder(findSourceDir(data.fromDir), data.fromTos, new File(data.toDir)) execute
  }

  private def findAllTemplates(): List[TemplateData] = {
    val jsonfiles = findTemplateDescriptors
    val templates: List[TemplateData] = jsonfiles.map(f => getTemplateData(f))
    templates
  }

  /**
   * return names of all *.template.json files
   */
  def findTemplateDescriptors(): List[File] = {
    FileUtils.findFiles(getTemplatesDir, file => file.getName().endsWith(TEMPLATE_FILTER))
  }

  /**
   * find the zipfile or source in the classpath or templates.dir folder
   */
  private def findSourceDir(filename: String): File = {
    val files = FileUtils.findFiles(getTemplatesDir, file => file.getName.equals(filename))
    assert(files != null && files.size > 0, "resource not found: " + filename)
    files(0)
  }

  /**
   * parse template data file to object
   */
  private def getTemplateData(file: File): TemplateData = {
    parse[TemplateData](FileUtils.readFile(file))
  }

  private def getTemplateName(file: File): String = {
    val s = file.getName()
    s.substring(0, s.indexOf("."))
  }

  private def getTemplatesDir(): File = {
    val sysvar = System.getProperty(TEMPLATES_DIR)
    if (sysvar != null && sysvar.trim.length > 0) {
      println("using templates dir " + sysvar)
      new File(sysvar.trim)
    } else {
      FileUtils.getClasspathBinDir
    }
  }
}