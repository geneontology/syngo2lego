## 



### Runner (All files from one dump):  

Parses a set of models specified in a JSON file into a set of JSON files.
Load OWL file up here.


### Full model (one or more simple models in one file) :
 Wrapper for parsing one model worth of JSON, writing one OWL file.
 
### Simple model:  

Architecture:  Write this as a function?

Class model writer - takes in one model worth of SynGO JSON.
Generates OWL

simple model:  Class simple model 

one Uniprot ID -> Individual typed as ??
one primary GO term = Ind + GO type
one or more extensions:  Ind + type + relation to primary GO term.  Type may be from any number of OBO ontologies.
relation will be specified using shortcut name.  So will need a dictionary to look these up.
