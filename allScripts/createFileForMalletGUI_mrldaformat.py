import sys
import scipy.io
import numpy as np

if len(sys.argv) < 2:
	print "usage: mrlda file, output dir"
	sys.exit()

input_file = sys.argv[1]
output_file = sys.argv[2]
with open(input_file, 'r') as file:
        for line in file:
		entries = line.rstrip().split('\t')
		newFile = open(output_file+"/"+entries[0].replace("/",""),'w')
		newFile.write(entries[1])
		newFile.close()

