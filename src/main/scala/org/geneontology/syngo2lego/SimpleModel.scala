package org.geneontology.syngo2lego

import rapture.json._, jsonBackends.jawn._
import org.phenoscape.scowl._
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.search.EntitySearcher
import dosumis.brainscowl.BrainScowl
import org.semanticweb.owlapi.apibinding.OWLManager
import scala.language.postfixOps
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import collection.JavaConverters._
import java.util.Date
import java.text.SimpleDateFormat

class SimpleModel (val model_ns: String, var ont : BrainScowl, 
                   val jmodel : Json, val go: BrainScowl) {
  // Passing rather too many vars.  Deeper class model with some 
  // inheritance might be good here instead...
  
  // Declare globals
  
  // Namespaces (In the OWL sense - AKA Base IRIs)
  val obo_ns = "http://purl.obolibrary.org/obo/"
  val idOrg_ns = "http://identifiers.org/"
  val synGO_ns = "http://syngo.vu.nl/"
  
 // OP vals    // This should really be pulled from a config file
  val enabled_by = ObjectProperty(obo_ns + "RO_0002333")
  val part_of = ObjectProperty(obo_ns + "BFO_0000050")
  val occurs_in = ObjectProperty(obo_ns + "BFO_0000066")
  // Lookup needed to translate identifiers from JSON
  val OP_lookup = Map( "part_of" -> part_of, 
      "occurs_in" ->  occurs_in)  // More concise way to do this?
      
  val primary_class = Class(obo_ns + this.jmodel.goTerm.as[String].replace(":", "_"))
  
  // Add check that primary class exists in GO!
      
  // Annotations // Global 'cos they go on everything.
      
  val dc_contributor = AnnotationProperty("http://purl.org/dc/elements/1.1/contributor")
//  val contributor = IRI.create(synGO_ns + jmodel.username.as[String])
  val comment = AnnotationProperty("http://www.w3.org/2000/01/rdf-schema#comment")
  val contributor = "SynGO:" + jmodel.username.as[String]
  val source = "PMID:" + jmodel.pmid.as[String]
  val evidence = AnnotationProperty("http://geneontology.org/lego/evidence")    

  this.ont.annotateOntology(Annotation (dc_contributor, contributor))
  val dc_date = AnnotationProperty("http://purl.org/dc/elements/1.1/date")
  
  val species = jmodel.comments.species.as[String]
  val rationale = jmodel.comments.rationale.as[String]
  this.ont.annotateOntology(Annotation (comment ,
      s"Rationale: ${rationale}. Experimental description: ${species}"))
  
  // Temporary expedient
  def date(): String = {
    val now = new Date()
    val ft = new SimpleDateFormat("yyyy-MM-dd")
    return ft.format(now)
  }
 
  // methods for generating and adding new inds.  All return ind.

  
  // Lookup OBO namespace of primary term.  Use Ontology to do this.
  // Do NOT use goDomain from the JSON as MFs are mislabelled as BPs:
  // e.g. This labeled as BP	GO:1905056	calcium-transporting ATPase activity involved in ...
 
  val primary_aspect = go.getSpecTextAnnotationsOnEntity(
      query_short_form = this.jmodel.goTerm.as[String].replace(":", "_"),
      ap_short_form = "hasOBONamespace").head // head bakes in cardinality without check!

  
  def new_ind() : OWLNamedIndividual = {
    val i = NamedIndividual(this.model_ns + java.util.UUID.randomUUID.toString)
    this.ont.add_axiom(i Annotation (dc_contributor, contributor)) // Also needs date.
    this.ont.add_axiom(i Annotation (dc_date, date)) // Also needs date.    
    return i
  }
  
  // The following could be made into a generic method that gets passed an ind gen fn
  // or more simply - a URI gen function or just an IRI string.
  def new_typed_ind(typ: String): OWLNamedIndividual = {
    val i = new_ind()
    this.ont.add_axiom(i Type Class(typ))
    return i
  }
  
  def new_mfi() : OWLNamedIndividual = {
     return new_typed_ind(obo_ns + "GO_0003674")  //molecular function.
  }
  
  def new_gp(): OWLNamedIndividual = {
//  return new_typed_ind(obo_ns + "UniProtKB_" + this.jmodel.uniprot.as[String])
    return new_typed_ind(idOrg_ns + "uniprot/" + this.jmodel.uniprot.as[String])
  }

  def new_primary_ind(): OWLNamedIndividual = {
    return new_typed_ind(obo_ns + this.jmodel.goTerm.as[String].replace(":", "_"))
  }
  
  
  // Annotations on edges get attached to individuals - which are then used to annotate the edge
// "evidence": { "system": [
//                     [
//                        "biosys:intacttissue"
//                     ]
//                  ],
//                  "assay": [
//                     [
//                        "ECO:0006003"
//                     ]
//                  ],
//                  "target": [
//                     [
//                        "target:antibody"
//                     ]
//                  ]
//               },
 
  def gen_annotations(evj: Json): Set[OWLAnnotation] = {
    // Generate a set of individuals to be attached to edges as evidence
    // Also annotates model with contributor
    // Should probably add these to the ontology too - but feels like wrong place to do it.
  ///  this.ont.add_axiom(ont.ontology Annotation(dc_source, source))
     val dc_source = AnnotationProperty("http://purl.org/dc/elements/1.1/source")
     var out = Set[OWLAnnotation]()  
       for ((k,v) <- jmodel.evidence.as[Map[String, Json]]) {
         // TODO annotated inds with syngo evidence codes. Needs extension to JSON.
         for (eco <- v.as[List[String]]) {  //
           // Add in conditional to check for ECO by regex match.  Warn if not.
           val re = "^ECO:[0-9]{7}$".r
           val m = re.findFirstIn(eco)
           if (!m.isEmpty) {
             val ann = new_typed_ind(obo_ns + eco.replace(":", "_")) // 
             this.ont.add_axiom(ann Annotation (dc_source, source))
             out += Annotation(evidence, ann)
           } else {
             println(s"Ignoring ${eco} as it doesn't look like an ECO term.")
           }
         }
       }
     return out
    }
  
    
 // primary entity   
  // Add extensions
  
  def gen_simple_annotation(): OWLNamedIndividual = {  
    // Generates a standard LEGO annotation, returns primary ind for use in extension.
    // TBA: Axiom annotation.
      val gp =  new_gp()
      val primary_ind = new_primary_ind()
   // ObjectPropertyAssertion(annotations, loves, Aya, Freddy)     // For ref
    if (primary_aspect == "cellular_component") {
      val mfi = new_mfi()
      this.ont.add_axiom(ObjectPropertyAssertion(gen_annotations(jmodel.evidence), 
                                                  enabled_by, mfi, gp))
      this.ont.add_axiom(ObjectPropertyAssertion(gen_annotations(jmodel.evidence),
                                                  occurs_in, mfi, primary_ind))
    }
    if (primary_aspect == "molecular_function") {
      val annotations = gen_annotations(jmodel.evidence)
      this.ont.add_axiom(ObjectPropertyAssertion(annotations, enabled_by, primary_ind, gp))       
    }  
  //    this.ont.add_axiom(ObjectPropertyAssertion(annotations, gp, occurs_in, mfi)  Ask!
    if (primary_aspect == "biological_process") {
      val mfi = new_mfi()
      this.ont.add_axiom(ObjectPropertyAssertion(gen_annotations(jmodel.evidence)
                                                 , enabled_by, mfi, gp))
      this.ont.add_axiom(ObjectPropertyAssertion(gen_annotations(jmodel.evidence)
                                                 , part_of, mfi, primary_ind))
    }
  return primary_ind
  }
      
//  for ref
// fu["SynGO"][2]['models'][0]['extensions']
// [{'part_of': ['UBERON:0001869']}, {'part_of': ['UBERON:0002421']}]
// [{'occurs_in': ['UBERON:0000955', 'Glutamatergic']}, {'occurs_in': ['UBERON:0000955', 'GABAergic']}]
  
// This second example =>  two separate annotations
// Note - could potentially be improved by assuming Uberon part_of GO.
  
  
  // if none create one annotation, other wise create same number as length.


  val extensions = jmodel.extensions.as[List[Json]]

  def extend(primary_ind: OWLNamedIndividual, extension: Json){
    // Checks Json, uses it to extend pimary ind
    val ext = extension.as[Map[String, List[String]]]
    for ((k,v) <- ext) { 
      val rel = OP_lookup(k)
      for (o <- v) {
        val oi = new_typed_ind(obo_ns + o.replace(":", "_"))
        this.ont.add_axiom(ObjectPropertyAssertion(gen_annotations(jmodel.evidence),
                                                  rel,primary_ind, oi)) 
      }
    }
  }
  
 
  def generate() {
    // Adds simple model set of annotations to LEGO model ont:
    // Each extension set gets a new_ind/ GP pair.
    if (extensions.isEmpty) {
      gen_simple_annotation  // No extension needed. Return value discarded
    }
    else { 
      for (e <- extensions) {
        val primary_ind = gen_simple_annotation()
        extend(primary_ind, e)
      }
    }
  }
  

    // If MF
    // IF BP
  // else:
//    (Man SubClassOf Person) Annotation (RDFSComment, "States that every man is a person.")

    
  // Loop over extends adding inds and relationships

}
