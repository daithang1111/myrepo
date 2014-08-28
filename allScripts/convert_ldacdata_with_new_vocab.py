import os
import sys
import scipy.io
import numpy as np

if len(sys.argv) < 3:
	print "usage: ldac test file, old vocab, new vocab"
	sys.exit()

test_file = sys.argv[1]
old_vocab = sys.argv[2]
new_vocab = sys.argv[3]

# Read in the vocabulary and build a symbol table mapping words to indices
table_old = dict()
numwords = 0
with open(old_vocab, 'r') as file:
    for line in file:
        table_old[line.rstrip()] = numwords
        numwords += 1

# Read in the vocabulary and build a symbol table mapping words to indices
table_new = dict()
numwords = 0
with open(new_vocab, 'r') as file:
    for line in file:
        table_new[table_old[line.rstrip()]] =numwords
	numwords += 1

#read test file
output =open(test_file+".new", 'w')
with open(test_file, 'r') as file:
	for line in file:
		word_freq =line.rstrip().split(" ")		
		term_len =int(word_freq[0])
		new_term_len=0
		new_doc=""
		for t in xrange(term_len):
			wf = word_freq[t+1].split(":")
			w =int(wf[0])
			if w in table_new:
				#this term is in new vocab
				new_term_index=table_new[w]
				new_doc =new_doc+" "+str(new_term_index)+":"+wf[1]
				new_term_len+=1	
		if new_term_len>2:
			new_doc =str(new_term_len)+new_doc
			output.write(new_doc+'\n')
output.close()
