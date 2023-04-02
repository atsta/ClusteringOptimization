with open('2psl/dblp_dataset_shuffled.txt.binedgelist', 'rb') as f:
    data = f.read()

# Convert binary data to integer pairs
edges = []
for i in range(0, len(data), 8):
    source = int.from_bytes(data[i:i+4], byteorder='little')
    target = int.from_bytes(data[i+4:i+8], byteorder='little')
    edges.append((source, target))

# Save as text file
with open('Datasets/Converted/dblp_dataset_shuffled.csv', 'w') as f:
    for source, target in edges:
        f.write('{},{}\n'.format(source, target))
