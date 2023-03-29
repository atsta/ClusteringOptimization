# import numpy as np
# import pandas as pd

# # read in the binary edgelist file using numpy
# edges = np.loadtxt('2psl/amazon_dataset.txt.binedgelist', dtype=np.int32)

# # convert the binary edgelist file to a pandas DataFrame
# df = pd.DataFrame(edges, columns=['Source', 'Target'])

# # save the pandas DataFrame as a CSV file
# df.to_csv('Datasets/Converted/amazon_dataset_1.csv', index=False)


with open('2psl/amazon_dataset.txt.binedgelist', 'rb') as f:
    data = f.read()

# Convert binary data to integer pairs
edges = []
for i in range(0, len(data), 8):
    source = int.from_bytes(data[i:i+4], byteorder='little')
    target = int.from_bytes(data[i+4:i+8], byteorder='little')
    edges.append((source, target))

# Save as text file
with open('Datasets/Converted/amazon_dataset.csv', 'w') as f:
    for source, target in edges:
        f.write('{},{}\n'.format(source, target))
