ProjectBuilder-Scala
====================

ProjectBuilder-Scala


This app helps creating a new project structure by copying an existing template and doing some clever search-replace's 
of all from-to values that are defined in the corresponding template info file. 
With 'clever' is meant that not only strings are substituted in ascii files (case-sensitive), but also target folder(s) are renamed accordingly 
where a folder may be indicated using the dot notation. So:
from: aaa.bbb to: xxx.yyy.com will substitute all occurences of aaa.bbb -> xxx.yyy.com and also rename the source (sub) folder aaa/bbb to xxx/yyy/com

This application start with a small gui, showing a combobox with the names of all templates files that it found.
A template file, is a json file with a name like: 
xxxxx.templates.json

The programs looks for such template files in either the current classpath or the directory indicated  with the -Dtemplates.dir VM argument.
For example: -Dtemplates.dir="./templates"

An example template info file (example.template.json) looks like:
{
  "name"            : "Demo",
  "description"     : "This is another example, using directory",
  "fromDir"         : "TemplateWizzard",
  "toDir"           : "n.a.",
  "fromTos" : {
      "TemplateWizzard"  : "n.a.",
      "WizzardModel"     : "n.a.",
      "template.wizzard" : "n.a."
   }
}
The "n.a." indicates that these values will be filled in later via the Gui by the user.
You may add as many additional fromTo elements as needed.

When all field are filled in by the user, the [Finish] button will enabled, and after hitting that button, the source project is 
substituted and copied to the target location.

To create your own template, do the following:
First copy a directory or set of directories that you want to as the base, to some temp folder.
There, remove all files that you no need, so that you end up with a clean folder structure that can be used to kick-start a next project.
Copy one of the xxx.template.json files, and edit this accordingly. Note the fromDir can be either the root of your new template, 
or the name of the zipfile that you created from the template. Also note that you can add more 'fromTos' if needed.
That's all.

Compile & Build Notes:
To get rid of compile errors: 
define java classpath variable M3_REPO that points to the /m3-repo folder (that is part of this project)
To build a standalone runable jar file execute "ant" from the install dir.

To run this application from Eclipse:
either Scala-run ProjBuilderMain 
or Java-run ProjBuildJavaMain
or use the command-line script: projbuild.bat  or projbuild.sh
 

