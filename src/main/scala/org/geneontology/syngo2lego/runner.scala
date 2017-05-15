package org.geneontology.syngo2lego
import rapture.json._, jsonBackends.jawn._
import scala.io.Source
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import dosumis.brainscowl.BrainScowl
import java.lang.IllegalArgumentException

object runner extends(App) {
  // Takes full JSON file as input, splits it up into models.
  // Generates individual OWL files from each model
  // TODO: switch arg processing to standard module e.g. https://github.com/backuity/clist
   /** Takes 1 arg: path to synGO JSON file. 
    *  Or optionally specify */
  val test = true // Should be arg
  var imports_stat = true
  var SynGO_filepath = ""
  if (args.length == 2) {
      if (args(0) == "-ni") {
        imports_stat = false
        SynGO_filepath = args(1)
      } 
      else {
           new IllegalArgumentException("Unrecognized arg: " + args(0) +  "Should be one of -ni, ...")
           // Should throw exception.
      }
  }
  else if (args.length == 1) {
     SynGO_filepath = args(0)
  } 
  else {
     throw new IllegalArgumentException("Wrong number of args: " + args.length)
     // Should throw exception.
   }
  val go = new BrainScowl("resources/go-simple.ofn")  
  val synGO_file = Source.fromFile(SynGO_filepath).getLines.mkString
  val synGO_json = Json.parse(synGO_file)
  // At this point - should run check of json - outer struc
  // Loop over models calling genModel
  for (a <- synGO_json.SynGO.as[List[Json]]) {
  var lm = new LegoModel(a, go, imports_stat)
      }
  go.sleep()
}