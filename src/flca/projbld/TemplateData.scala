package flca.projbld

/**
 * class that will be populated from the json text file that is selected by the user
 * @author nly36776
 *
 */
case class TemplateData (
  var name			: String,
  var description 	: String,
  var fromDir		: String,
  var toDir			: String,
  var fromTos		: Map[String, String]
)