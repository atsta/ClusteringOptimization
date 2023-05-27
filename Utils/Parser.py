import gzip
import csv

# importcsv = csv.writer(open('amazon_dataset_1.csv', 'w'))
# with gzip.open('com-amazon.ungraph.txt.gz', "rt") as f:
#     f.readline()
#     f.readline()
#     f.readline()
#     f.readline()
#     for line in f:
#         fields = line.strip().split('\t')
#         row = [fields[0], fields[1]]
#         importcsv.writerow(row)

# with open('amazon_dataset_1.csv', newline='') as in_file:
#     with open('amazon_dataset.csv', 'w', newline='') as out_file:
#         writer = csv.writer(out_file)
#         for row in csv.reader(in_file):
#             if row:
#                 writer.writerow(row)

importcsv = csv.writer(open('Datasets\journal_dataset_1.csv', 'w'))
with gzip.open('com-lj.ungraph.txt.gz', "rt") as f:
    f.readline()
    f.readline()
    f.readline()
    f.readline()
    for line in f:
        fields = line.strip().split('\t')
        row = [fields[0], fields[1]]
        importcsv.writerow(row)

#remove empty lines
with open('Datasets\journal_dataset_1.csv', newline='') as in_file:
    with open('Datasets\journal_dataset.csv', 'w', newline='') as out_file:
        writer = csv.writer(out_file)
        for row in csv.reader(in_file):
            if row:
                writer.writerow(row)