import sys
import scipy.io
import numpy as np
import math
if len(sys.argv) < 3:
	print "usage: mallet output, vocab, beta file"
	sys.exit()

mallet_output_file = sys.argv[1]
vocab_file = sys.argv[2]
beta_file = sys.argv[3]

#read vocab first
table =dict()
numwords = 1
with open(vocab_file, 'r') as file:
	for line in file:
		table[line.rstrip()] =numwords
		numwords+=1
#output beta file
output = open(beta_file, 'w')

#read mallet output file
onerow =dict()
currentTopic=0
sumARow=0
with open(mallet_output_file, 'r') as file:
	for line in file:
		topic, word, weight =[x for x in line.rstrip().split('\t')]
		if currentTopic !=int(topic):
			print "Print the row :", str(currentTopic)
			for aWord in table:
				if aWord not in onerow:
					print "This is a bug"
					break
				log_weight = math.log((float(onerow[aWord])/float(sumARow))+0.1e-100)
				output.write(" "+str(log_weight))
			output.write("\n")
			currentTopic=int(topic)
			sumARow = float(weight)
			onerow[word] = weight
		else:
			sumARow+=float(weight)
			onerow[word] =weight		

#print the last topic
print "print the last topic"
for aWord in table:
	log_weight = math.log((float(onerow[aWord])/float(sumARow))+0.1e-100)
        output.write(" "+str(log_weight))

output.close()	
