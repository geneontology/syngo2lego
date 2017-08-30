package org.geneontology.syngo2lego
import org.semanticweb.owlapi.apibinding.OWLManager
import dosumis.brainscowl.BrainScowl
import org.phenoscape.scowl._

import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.search.EntitySearcher
import rapture.json._, jsonBackends.jawn._
import scala.io.Source
import java.io.File
import java.util.Date
import java.text.SimpleDateFormat

class LegoModel (val jmodel : Json, val GO : BrainScowl, add_import_statement: Boolean) {
  /**
  
  Take one model worth of SynGO JSON (as Json)
  Generates a new, Turtle format ontology file representing this JSON as LEGO.
  GO = a BrainScowl object wrapping GO to use for looking up entities.
  - If add_import_statement is True, it generates a file for loading into noctua:
  Import statement for go-lego.owl . Filename has no extension.
  - If add_import_statement is False, it doesn't add an import statement and saves a file with a
  .ttl extension.  This file is useful for local testing. */
 
  // Architecture: Doesn't need to be a class... could just be an object with a main function.
  // Has two modes:  
//val test = "_test" // switch for test mode - flags filenames and metadata. // should be from arg.  
  val test = "" // switch for test mode - flags filenames and metadata. // should be from arg.
  val syngo_id = this.jmodel.combi_id.as[String].replace(":", "_")
  println(s"Processing ${syngo_id}.")
  val base = "http://model.geneontology.org/"
  var owl_model = new BrainScowl(iri_string = base + syngo_id + test, base_iri = base + syngo_id) // constructor may change
  val title = AnnotationProperty("http://purl.org/dc/elements/1.1/title")
  val dc_date = AnnotationProperty("http://purl.org/dc/elements/1.1/date")
  val model_status = AnnotationProperty("http://geneontology.org/lego/modelstate")
  owl_model.annotateOntology(Annotation(title, syngo_id + test))
  // Files have no assoc date (except for in comments). So, for now at least,
  // generating here to fulfill loading requirements.
  
  val datestamp = this.jmodel.datestamp.as[String]
  owl_model.annotateOntology(Annotation(dc_date, datestamp))
  owl_model.annotateOntology(Annotation(model_status, this.jmodel.status.as[String]))
  val mods = this.jmodel.models.as[List[Json]]
  val model_ns = base + syngo_id
  var file_extension = ""
  for (mod <- mods) { 
    // Add in check of JSON integrity mod
    var sm = new SimpleModel(model_ns, owl_model, mod, GO)
    sm.generate()
  }

  if (this.add_import_statement) {
    owl_model.add_import("http://purl.obolibrary.org/obo/go/extensions/go-lego.owl")
    file_extension = ".ttl"
  }
  owl_model.save(syngo_id + test + ".ttl", "ttl")  // 
  owl_model.sleep()
}