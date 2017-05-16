package org.geneontology.syngo2lego
import rapture.json._, jsonBackends.jawn._
import scala.io.Source
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import dosumis.brainscowl.BrainScowl
import java.lang.IllegalArgumentException
import org.backuity.clist._


class Cat extends Command(description = "fubar fubar fubar") {
  var no_imports = opt[Boolean](abbrev = "ni", description = "No imports", default = true)
  var development = opt[Boolean](abbrev = "dev", description = "Development status in noctua")
  var publish = opt[Boolean](abbrev = "pub", description = "Publish status in noctua.")  
  var json_file = arg[String](description = "SynGO JSON file")
}

object runner extends(App) {
  Cli.parse(args).withCommand(new Cat) { case cat => 
  // Takes full JSON file as input, splits it up into models.
  // Generates individual OWL files from each model
  // TODO: switch arg processing to standard module e.g. https://github.com/backuity/clist
   /** Takes 1 arg: path to synGO JSON file. 
    *  Or optionally specify */
  val go = new BrainScowl("resources/go-simple.ofn") // TODO switch to pulling this dynamically from URL
  val synGO_file = Source.fromFile(cat.json_file).getLines.mkString
  val synGO_json = Json.parse(synGO_file)
  // At this point - should run check of json - outer struc
  // Loop over models calling genModel
  var status = "delete"
  if (cat.development) {
    status = "development"
  }
  if (cat.publish) {
    status = "publish"
  }
  for (a <- synGO_json.SynGO.as[List[Json]]) {
  var lm = new LegoModel(a, go, cat.no_imports, status)
      }
  go.sleep()
  }
}