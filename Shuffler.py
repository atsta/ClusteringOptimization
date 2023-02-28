import csv
import random

#shuffle dataset
csv_file = open('Datasets/amazon_dataset.csv', 'r')
csv_reader = csv.reader(csv_file)
csv_data = list(csv_reader)
random.shuffle(csv_data)
csv_file = open('Datasets/amazon_dataset_shuffled_1.csv', 'w')
csv_writer = csv.writer(csv_file)
for row in csv_data:
    csv_writer.writerow(row)
csv_file.close()

#remove empty lines
with open('Datasets/amazon_dataset_shuffled_1.csv', newline='') as in_file:
    with open('Datasets/damazon_dataset_shuffled.csv', 'w', newline='') as out_file:
        writer = csv.writer(out_file)
        for row in csv.reader(in_file):
            if row:
                writer.writerow(row)