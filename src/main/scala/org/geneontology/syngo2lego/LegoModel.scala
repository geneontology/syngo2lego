package org.geneontology.syngo2lego
import org.semanticweb.owlapi.apibinding.OWLManager
import org.dosumis.brainscowl.BrainScowl

import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.search.EntitySearcher
import rapture.json._, jsonBackends.jawn._
import scala.io.Source
import java.io.File

class LegoModel (var jmodel : Json, var GO : BrainScowl) { 
  
  // Take one model worth of JSON (as )
  // Generates new ontology representing this JSON as LEGO.
  // Saves as file (Could be split out to runner?)
  
  // Sketch of meta-data ontology 
   
  val syngo_id = this.jmodel.model_id.as[String].replace(":", "_")
  val base = "http://model.geneontology.org/"
  var owl_model = new BrainScowl(iri_string = base + syngo_id + ".owl", base_iri = base + syngo_id) // constructor may change
  val mods = this.jmodel.models.as[List[Json]]
  val model_ns = base + syngo_id
  for (mod <- mods) { 
    // Add in check of JSON integrity mod
    var sm = new SimpleModel(model_ns, owl_model, mod, GO)
    sm.generate()
  }
  // TODO - add ontology level annotations:
  owl_model.save(syngo_id + ".owl")
  owl_model.sleep()
}