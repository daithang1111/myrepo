import sys
import scipy.io
import numpy as np
import math
if len(sys.argv) < 1:
	print "usage: likelihood fie"
	sys.exit()

l_file = sys.argv[1]

num_docs =0
l_sum =0
with open(l_file, 'r') as file:
	for line in file:
		num_docs+=1
		l_sum+= float(line.rstrip())
#output corpus to file
output = open(l_file+".avg", 'w')
l_sum = l_sum/float(num_docs)
print "num_docs: ", num_docs
print "avg likelihood: ", str(l_sum)
output.write(str(l_sum))
output.close()
