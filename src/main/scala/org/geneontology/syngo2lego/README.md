## Outline

### Main:  

Scala app that parses a set of models specified in a JSON file into a set of OWL (ttl) files.
Load reference OWL file(s) here.

###  LegoModel (one or more simple models in one file):

Wrapper for parsing a noctual model consisting of one or more simple models (corresponding to classic OWL annotations) and writing a single OWL (ttl) file.  Currently there are no connections between simple models in SynGO JSON.  Calls SimpleModel to generate component classic GO annotations.
 
### SimpleModel:  

Generate a single classic extended GO annotation, annotated as for LEGO.

