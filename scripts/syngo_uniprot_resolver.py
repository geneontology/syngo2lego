import json
import requests
import argparse
from pandas import read_csv
from io import StringIO
from os import path
from uniprot_wrapper import UniprotWrapper

p = argparse.ArgumentParser()
p.add_argument('-f', "--filename", type=str, required=True, help="input filename of SynGO annotation export JSON")
p.add_argument('-o', "--outfile", type=str, required=False, help="output filename")

def main():
    args = p.parse_args()

    filename = args.filename
    with open(filename) as f:
        data = json.load(f)

    wrapper = UniprotWrapper()
    extensions = []
    ext_genes = []
    ext_sets = []
    id_map = {}

    for a in data["SynGO"]:
        for m in a['models']:
            uniprot_id = m["uniprot"]
            if uniprot_id not in id_map:
                id_map[uniprot_id] = {}

    id_map = wrapper.lookup_uniprot(list(id_map.keys()))

    # <http://rgd.mcw.edu/rgdweb/report/gene/main.html?id=620107>
    # <http://www.informatics.jax.org/accession/MGI:MGI:95615>

    for a in data["SynGO"]:
        models = []
        for m in a['models']:
            uniprot_id = m['uniprot']
            if id_map[uniprot_id] == {}:
                uniprot_id = uniprot_id.split("-")[0] # Adjust for isoforms
            noctua_gene_id = wrapper.get_noctua_gene_id(id_map, uniprot_id)
            if noctua_gene_id is None:
                noctua_gene_id = m["uniprot"]
            m["noctua_gene_id"] = noctua_gene_id
            resulting_dbs = list(set(wrapper.DBS_TO_LOOKUP) & set(id_map[uniprot_id].keys()))
            if len(resulting_dbs) > 0:
                m["id_db_source"] = resulting_dbs[0]
            else:
                m["id_db_source"] = "uniprot"
            for field in UniprotWrapper.OTHER_FIELDS_TO_FETCH:
                if field in id_map[uniprot_id]:
                    m[field] = UniprotWrapper.get_field_for_id(id_map, field, uniprot_id)
            if "GENENAME" not in m:
                # Report missing info for this one
                print(m['uniprot'] + " - " + a['combi_id'] + " - " + str(UniprotWrapper.get_field_for_id(id_map, "GENENAME", uniprot_id)))
            ext_relations = []
            for e in m['extensions']:
                for k in e.keys():
                    go_terms = []
                    uberon_terms = []
                    other_terms = []
                    if k not in extensions:
                        extensions.append(k)
                    if k not in ext_relations:
                        ext_relations.append(k)
                    for t in e[k]:
                        if ":" not in t and t not in ext_genes:
                            ext_genes.append(t)
                        if t.startswith("GO:"):
                            go_terms.append(t)
                        elif t.startswith("UBERON:"):
                            uberon_terms.append(t)
                        else:
                            other_terms.append(t)
                    # if len(go_terms) > 1 or len(uberon_terms) > 1 or len(other_terms) > 0:
                    #     ext_sets.append([a["combi_id"],e[k]])
            if len(set(ext_relations)) > 1:
                ext_sets.append(a["combi_id"])
            models.append(m)
        a['models'] = models

    outfile = args.outfile
    if outfile is None:
        outfile = path.splitext(filename)[0] + "_updated" + path.splitext(filename)[1]
    with open(outfile, "w+") as wf:
        json.dump(data, wf, indent=4)

if __name__ == "__main__":
    main()