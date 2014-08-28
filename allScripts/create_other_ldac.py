import sys
import scipy.io
import numpy as np

if len(sys.argv) < 3:
	print "usage: number of topics, A file, other file"
	sys.exit()

num_topics = int(sys.argv[1])
A_file = sys.argv[2]
other_file = sys.argv[3]

#read beta file to get num of terms
num_terms =0
with open(A_file, 'r') as file:
        for line in file:
                num_terms+=1

#read vocab first
alpha = 0.1 #float(60)/float(num_terms)

output = open(other_file,'w')
output.write("num_topics "+str(num_topics)+"\n")
output.write("num_terms "+str(num_terms)+"\n")
output.write("alpha "+str(alpha))
output.close()	
