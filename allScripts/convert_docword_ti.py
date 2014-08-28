import sys
import scipy.io
import numpy as np

if len(sys.argv) < 3:
	print "usage: docword, vocab, ti_file"
	sys.exit()

docword_file = sys.argv[1]
vocab_file = sys.argv[2]
ti_file = sys.argv[3]

infile = file(docword_file)
num_docs = int(infile.readline())
num_words = int(infile.readline())
nnz = int(infile.readline())

#read vocab first
table =dict()
numwords = 1
with open(vocab_file, 'r') as file:
	for line in file:
		table[numwords] = line.rstrip()
		numwords+=1
#output corpus to file
output = open(ti_file, 'w')

#read docword
corpus =dict()
for l in infile:
        d, w, v = [int(x) for x in l.split()]
	for i in xrange(v):
		if d in corpus:
			corpus[d] = corpus[d]+' '+table[w]
		else:
			corpus[d] =table[w]

for c in corpus:
	output.write(corpus[c]+'\n')
output.close()	
