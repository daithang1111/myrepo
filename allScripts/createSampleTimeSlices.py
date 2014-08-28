import sys
import scipy.io
import numpy as np

if len(sys.argv) < 3:
	print "usage: ldac file, number of slices, output"
	sys.exit()

input_file = sys.argv[1]
slices = int(sys.argv[2])
output_file = sys.argv[3]
if slices<1:
	sys.exit()
total_docs =0
with open(input_file, 'r') as file:
        for line in file:
                total_docs+=1

output =open(output_file,'w')
output.write(str(slices)+"\n")

num_in_slice = total_docs/slices
for i in xrange(slices-1):
	output.write(str(num_in_slice)+"\n")

#write last slice
last_slice =total_docs - num_in_slice*(slices-1)

output.write(str(last_slice)+"\n")
output.close()
