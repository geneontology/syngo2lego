package org.geneontology.syngo2lego
import rapture.json._, jsonBackends.jawn._
import scala.io.Source
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import dosumis.brainscowl.BrainScowl

object runner extends(App) {
  // Takes full JSON file as input, splits it up into models.
  // Generates individual OWL files from each model
   /** Takes 1 arg: path to synGO JSON file. */
      if (args.length == 1) {
      val go = new BrainScowl("resources/go-simple.owl")  
      val synGO_file = Source.fromFile(args(0)).getLines.mkString
      val synGO_json = Json.parse(synGO_file)
    // At this point - should run check of json
  // Loop over models calling genModel
    for (a <- synGO_json.SynGO.as[List[Json]]) {
    var lm = new LegoModel(a, go)
      }
    go.sleep()
   } else {
     println("Wrong number of args: " + args.length)
   }
}