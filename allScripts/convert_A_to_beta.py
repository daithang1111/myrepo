import sys
import scipy.io
import numpy as np
import math
if len(sys.argv) < 3:
	print "usage: num of topics, anchor A file (word-topic), ldac beta file (topic-word dist)"
	sys.exit()

#num_words = int(sys.argv[1])
num_topics = int(sys.argv[1])
A_file = sys.argv[2]
beta_file = sys.argv[3]

num_words =0
with open(A_file, 'r') as file:
	for line in file:
		num_words+=1
	
A_matrix ={} # scipy.sparse.lil_matrix((num_words, num_topics))
topic=0
word=0

with open(A_file, 'r') as file:
	for line in file:
		l = line.rstrip().split(" ")
		for topic in xrange(len(l)):
			A_matrix[word,topic] =math.log(float(l[topic])+0.1e-100)
		word+=1
#output corpus to file
output = open(beta_file, 'w')
t_sum =0
for topic in xrange(num_topics):
	t_sum =0
	for word in xrange(num_words):
		t_sum+=math.exp(A_matrix[word,topic])
		if word ==0:
			output.write(str(A_matrix[word,topic]))
		else :
			output.write(" "+str(A_matrix[word,topic]))
	print "sum of topic ", topic, ": ", str(t_sum)
	output.write("\n")
output.close()
