import sys
import scipy.io
import numpy as np

if len(sys.argv) < 3:
	print "usage: number of topics, vocab size, other file"
	sys.exit()

num_topics = int(sys.argv[1])
num_terms = sys.argv[2]
other_file = sys.argv[3]


#read vocab first
alpha = 0.014 #float(60)/float(num_terms)

output = open(other_file,'w')
output.write("num_topics "+str(num_topics)+"\n")
output.write("num_terms "+str(num_terms)+"\n")
output.write("alpha "+str(alpha))
output.close()	
