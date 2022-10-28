import gzip
import csv

importcsv = csv.writer(open('dataset.csv', 'w'))
with gzip.open('dataset.gz', "rt") as f:
    f.readline()
    f.readline()
    f.readline()
    f.readline()
    for line in f:
        fields = line.strip().split('\t')
        row = [fields[0], fields[1]]
        importcsv.writerow(row)