package flca.projbld.gui

import java.io.File
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.events.ModifyEvent
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.layout.RowLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.DirectoryDialog
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Text
import flca.projbld.TemplateData
import flca.projbld.ProjectBuilder
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Event
import org.eclipse.swt.layout.RowData
import org.eclipse.swt.widgets.Control

/**
 * the class that will show swt based gui
 * @author nly36776
 *
 */
object TemplateGui extends flca.projbld.Constants {

  var shell: Shell = null;
  var fromToContainer: Composite = null
  var viewer: TableViewer = null
  var stack: Composite = null
  var stackLayout: StackLayout = null
  var stackForms = Map[String, Composite]()
  var combo: Combo = null
  var finishButton: Button = null
  var useData: TemplateData = null
  var targetDir = ""

  def show(templates: List[TemplateData]): TemplateData = {
    val display = new Display()
    shell = new Shell(display)
    setGridLayout(shell, 1)
    addCombo()
    addStack(templates)
    addFinishButton()
    displayIt(display)
    useData
  }

  private def addStack(templates: List[TemplateData]) = {
    stack = new Composite(shell, SWT.BORDER)
    stackLayout = new StackLayout();
    stack.setLayout(stackLayout)
    val griddata = new GridData()
    griddata.widthHint = 500
    stack.setLayoutData(griddata);
    templates.foreach(data => makeStackForm(data))
  }

  private def makeStackForm(data: TemplateData) = {
    var c = makeContainer(stack, 2)
    addDescription(c, data.description)
    addTargetDir(c)
    addStackHeader(c)
    addStackSubsFromTos(c, data)
    stackForms += data.name -> c
    combo.add(data.name)
  }

  private def addTargetDir(parent: Composite) = {
    var label = new Label(parent, SWT.NULL).setText("target dir")
    var c = new Composite(parent, SWT.NONE)
    c.setLayout(new RowLayout())

    var toDir = new Text(c, SWT.BORDER)
    setTextWithRow(toDir)
    toDir.setData(TO_DIR_KEY, TO_DIR_KEY)
    toDir.addListener(SWT.FocusOut, new Listener() {
      def handleEvent(e: Event) = finishButton.setEnabled(enableFinishBtn)
    })

    var b = new Button(c, SWT.PUSH)
    b.setText("..")
    b.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) = getTargetFromUser(e)
    })
  }

  private def addStackSubsFromTos(container: Composite, data: TemplateData) = {
    container.setData(DATA_KEY, data) // so that the execute() can obtain the corresponding data object
    data.fromTos.foreach(f => { addFromTo(container, f._1, f._2) })
  }

  private def addStackHeader(parent: Composite) = {
    var l1 = new Label(parent, SWT.NULL).setText("substitute this ..")
    val l2 = new Label(parent, SWT.NULL).setText("to this value")
  }

  private def addFromTo(parent: Composite, fromstr: String, tostr: String) = {
    var l = new Label(parent, SWT.NULL).setText(fromstr)
    val t = new Text(parent, SWT.BORDER)
    t.setData(FROM_STRING_KEY, fromstr)
    setTextWith(t)
    t.addListener(SWT.FocusOut, new Listener() {
      def handleEvent(e: Event) = finishButton.setEnabled(enableFinishBtn)
    })
  }

  private def addDescription(parent: Composite, descr: String) = {
    new Label(parent, SWT.NULL).setText("description")
    new Label(parent, SWT.NULL).setText(descr)
  }

  private def addCombo() = {
    var row = makeContainer(shell, 2)
    new Label(row, SWT.NULL).setText("Select template: ")
    combo = new Combo(row, SWT.NONE)
    combo.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) = {
        stackLayout.topControl = stackForms(combo.getText())
        stack.layout
        shell.pack
      }
    })
  }

  private def addFinishButton() = {
    var row = makeContainer(shell, 2)
    new Label(row, SWT.NULL).setText("")
    finishButton = new Button(row, SWT.PUSH)
    finishButton.setText("Finish")
    finishButton.setEnabled(false)
    finishButton.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(e: SelectionEvent) = { execute() }
    })
  }

  private def enableFinishBtn(): Boolean = {
    var result = targetDir.trim.length > 0

    if (result) {
      val c = stackLayout.topControl.asInstanceOf[Composite]
      c.getChildren.filter(_.isInstanceOf[Text]).foreach(t => {
        val txt = t.asInstanceOf[Text]
        if (txt.getText.trim.length == 0) result = false
      })
    }

    result
  }

  private def execute() = {
    val data = selectedData
    useData = new TemplateData(data.name, data.description, data.fromDir, targetDir, getFilledFromTos)
    shell.dispose
  }

  private def selectedData(): TemplateData = {
    val c = stackLayout.topControl.asInstanceOf[Composite]
    c.getData(DATA_KEY).asInstanceOf[TemplateData]
  }

  //how can we inprove this without mutable map ?
  private def getFilledFromTos(): Map[String, String] = {
    val c = stackLayout.topControl.asInstanceOf[Composite]
    var result = collection.mutable.Map[String, String]()
    c.getChildren.filter(_.isInstanceOf[Text]).foreach(t => {
      val txt = t.asInstanceOf[Text]
      val key = txt.getData(FROM_STRING_KEY).asInstanceOf[String]
      result(key) = txt.getText
    })

    result.toMap
  }

  private def makeContainer(parent: Composite, ncols: Int): Composite = {
    var r = new Composite(parent, SWT.NONE)
    setGridLayout(r, ncols)
    r
  }

  private def setTextWith(text: Text) = {
    val griddata = new GridData
    griddata.widthHint = TEXT_WIDTH
    text.setLayoutData(griddata)
  }

  private def setTextWithRow(text: Text) = {
    val griddata = new RowData
    griddata.width = TEXT_WIDTH
    text.setLayoutData(griddata)
  }

  private def setGridLayout(target: Composite, ncols: Int) = {
    val gridLayout = new GridLayout();
    gridLayout.numColumns = ncols;
    //    gridLayout.makeColumnsEqualWidth = true;
    target.setLayout(gridLayout)
  }

  private def displayIt(display: Display) = {
    shell.pack()
    shell.open()
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
  }

  private def getTargetFromUser(e: SelectionEvent) = {
    val dlg = new DirectoryDialog(shell);
    dlg.setText("SWT's DirectoryDialog");
    dlg.setMessage("Select a directory");
    val dir = dlg.open();
    if (dir != null) {
      targetDir = dir
      println(targetDir)
      getToDirTextControl.setText(targetDir)
      shell.layout
      shell.pack
    }
  }

  private def getToDirTextControl(): Text = {
    val c = stackLayout.topControl.asInstanceOf[Composite]
    val result = findControl(c, ctrl => ctrl.getData(TO_DIR_KEY) != null)
    if (result != null && result.isInstanceOf[Text]) result.asInstanceOf[Text]
    else null
  }

  private def findControl(c:Composite, fun: Control => Boolean): Control = {
    var result : Control = null
    def iter(c: Composite, fun: Control => Boolean): Unit = {
	    c.getChildren.filter(_.isInstanceOf[Control]).takeWhile(_ => result == null).foreach(ctrl => {
	      ctrl match {
	        case comp:Composite => iter(comp, fun)
	        case _ => if (fun(ctrl)) result = ctrl 
	      }
	    })
    }
    iter(c, fun)
    result
  }

}