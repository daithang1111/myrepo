#This script slip corpus into 15 70 15 percentage for DEV, TRAIN, TEST
import sys
import scipy.io
import numpy as np
import random
import re
import os
import glob
import math
import string

if len(sys.argv) < 3:
        print "usage: mrlda beta input, output file, vocab size"
        sys.exit()

input_file = sys.argv[1]
output_file = sys.argv[2]
vocab_size =int(sys.argv[3])

##########get beta in ldac format from sequence file format#########3
def getBeta(rawBeta, vSize):
	tmpBeta = rawBeta.replace("Value: {","").replace("}","")
	values = tmpBeta.split(", ")
	sum = 0.0
	pairDic = dict()
	for pair in values:
		index_value = pair.split("=")
		index =int(index_value[0])
		value =float(index_value[1])
		value =math.exp(value)
		pairDic[index] = value+0.1e-100
		sum+=value

	print "SUM is ",str(sum)
	output =list()	
	for i in xrange(vSize):
		realIndex = i+1
		if realIndex in pairDic:
			try:
				output.append(str(math.log(pairDic[realIndex]/sum)))
			except ValueError:
				print pairDic[realIndex]
		else:
			output.append(str(math.log(0.1e-100)))
	
	return " ".join(output)		


#########
output =open(output_file,"w")
count =0
index =0
#read all text file from dir
with open(input_file) as docFile:
        for docLine in docFile:
		if count >=5:
			if index%4 !=2:
				print "skip"
			else:
				print "get beta values"
				output.write(getBeta(docLine.rstrip(), vocab_size)+"\n")
			index+=1		
		count+=1
output.close()
