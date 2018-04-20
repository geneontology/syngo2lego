import json
import requests
import argparse
import logging
from pandas import read_csv
from io import StringIO
from os import path

p = argparse.ArgumentParser()
p.add_argument('-f', "--filename", type=str, required=True, help="input filename of space-separated UniProt ID list")
p.add_argument('-o', "--outfile", type=str, required=False, help="output filename for result JSON")
p.add_argument('-d', "--debug_level", type=str, required=False, help="logging level")

class UniprotWrapper():
    # DBS_TO_LOOKUP = ["MGI_ID", "RGD_ID", "FLYBASE_ID", "WORMBASE_ID", "HGNC_ID"]
    DBS_TO_LOOKUP = ["MGI_ID", "RGD_ID", "FLYBASE_ID", "WORMBASE_ID"]
    OTHER_FIELDS_TO_FETCH = ["GENENAME"]

    def make_uniprot_call(self, uniprot_ids, current_map=None):
        request_min = 0
        while request_min < len(uniprot_ids):
            for field in self.DBS_TO_LOOKUP + self.OTHER_FIELDS_TO_FETCH:
                r = requests.get('http://www.uniprot.org/uploadlists/?from=ACC&to=' + field + '&format=tab&query=' + " ".join(uniprot_ids[request_min:request_min+500]))
                uniprot_results = read_csv(StringIO(r.text), delimiter='\t')

                for index, row in uniprot_results.iterrows():
                    logging.debug(row[0] + " - " + field)
                    current_map[row[0]][field] = row[1]

            request_min += 5000 # For some reason requesting >1000 results in 400 error
        return current_map

    def lookup_uniprot(self, uniprot_ids, current_map=None, isoform_check=True):
        if current_map is None:
            current_map = {}
            for uid in uniprot_ids:
                current_map[uid] = {}
        current_map = self.make_uniprot_call(uniprot_ids, current_map)

        # Adjust for isoforms
        if isoform_check:
            redo_ids = []
            for k in current_map:
                if current_map[k] == {}:
                    redo_id = k.split("-")[0]
                    redo_ids.append(redo_id)
            for rid in redo_ids:
                current_map[rid] = {}
            current_map = self.make_uniprot_call(redo_ids, current_map)

        return current_map

    @staticmethod
    def one_off_call(uniprot_id):
        r = requests.get('http://www.uniprot.org/uniprot/' + uniprot_id + '.txt')
        return UniprotWrapper.get_gene_label(r.text.split("\n"))

    @staticmethod
    def get_gene_label(result_lines):
        gene_name = ""
        species = ""
        for line in result_lines:
            if line.startswith("GN   Name="):
                gene_name = line[5:len(line)].split(";")[0].split("{")[0]
                gene_name = gene_name[5:len(gene_name)].rstrip()
            elif line.startswith("OS"):
                species = line[5:len(line)]
                species = species.split(" ")
                species = species[0][0] + species[1][0:3]
        label = gene_name + " " + species
        return label

    @staticmethod
    def get_field_for_id(current_map, field, uniprot_id):
        if field in current_map[uniprot_id]: 
            return str(current_map[uniprot_id][field])

    def get_noctua_gene_id(self, current_map, uniprot_id):
        noctua_gene_id = None
        for db in self.DBS_TO_LOOKUP:
            if db in current_map[uniprot_id]:
                noctua_gene_id = UniprotWrapper.get_field_for_id(current_map, db, uniprot_id)
        return noctua_gene_id

def main():
    args = p.parse_args()
    
    if args.debug_level is not None:
        logging.basicConfig(level=args.debug_level)

    filename = args.filename
    outfile = args.outfile
    id_map = {}
    with open(filename) as f:

        wrapper = UniprotWrapper()    

        id_map = wrapper.lookup_uniprot(f.read().split(" "))

    if outfile is not None:
        with open(path.splitext(filename)[0] + "_output" + path.splitext(filename)[1], "w") as wf:
            json.dump(id_map, wf, indent=4)
    else:
        print(id_map)

if __name__ == "__main__":
    main()