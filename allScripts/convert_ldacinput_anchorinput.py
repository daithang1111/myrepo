import os
import sys
import scipy.io
import numpy as np

if len(sys.argv) < 2:
	print "usage: ldac input, anchor input"
	sys.exit()

ldac_file = sys.argv[1]
anchor_file = sys.argv[2]

#read vocab first
#table =dict()
dict_size =0
doc_count=0
word_count=0
nzz_count=0
term_len =0
output = open(anchor_file+'.tmp','w')
with open(ldac_file, 'r') as file:
	for line in file:
		doc_count+=1
		word_freq = line.rstrip().split(" ")
		term_len =int(word_freq[0])
		for t in xrange(term_len):
			wf = word_freq[t+1].split(":")
			tmp_wf0 =str(int(wf[0])+1) #anchor starts with 1, ldac with 0
			output.write(str(doc_count)+" " +tmp_wf0+" "+wf[1]+'\n')
			nzz_count +=1
			if dict_size <int(tmp_wf0):
				dict_size =int(tmp_wf0)
			#if tmp_wf0 not in table:
			#	table[tmp_wf0]=1

output.close()
output = open(anchor_file,'w')
output.write(str(doc_count)+'\n')
output.write(str(dict_size)+'\n')
output.write(str(nzz_count)+'\n')
with open(anchor_file+'.tmp','r') as file:
	for line in file:
		output.write(line)
output.close()
os.remove(anchor_file+'.tmp')
print 'Document count = ',doc_count
print 'word count = ',dict_size
print 'nzz count = ',nzz_count
