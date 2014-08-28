import sys
import scipy.io
import numpy as np

if len(sys.argv) < 3:
	print "usage: number of words, ldac output, ti output"
	sys.exit()

num_words = int(sys.argv[1])
ldac_file = sys.argv[2]
ti_file = sys.argv[3]

#read vocab first
table =dict()
count=0
num_topics =1
tmpStr =""
with open(ldac_file, 'r') as file:
	for line in file:
		line = line.strip()
		if len(line) > 0:			
			if count>0 and(count%(num_words+1) == 0):
				table[num_topics] = tmpStr
				tmpStr=""
				num_topics+=1
			elif count%(num_words+1) ==1:
				tmpStr=line
			else:
				tmpStr=tmpStr+" "+line
			count+=1
table[num_topics]=tmpStr

#output corpus to file
output = open(ti_file, 'w')

for c in table:
	output.write(table[c]+'\n')
output.close()	
