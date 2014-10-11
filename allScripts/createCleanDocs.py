#This script slip corpus into 15 70 15 percentage for DEV, TRAIN, TEST
import sys
import os
import glob
import math
import string

#read words
word_file ="words.tn"
vocab = dict()
index = 1
with open(word_file) as file:
	for line in file:
		vocab[index] = line.rstrip()
		index+=1


for fileLocation in glob.iglob(os.path.join('documents','*')):
	contents =list()
	with open(fileLocation) as docFile:
        	for docLine in docFile:
			contents.append(docLine.rstrip())

	#assume that contents always have 3 fields
	iLen = len(contents)/3

	newDocument = list()
	for i in xrange(iLen):
		index = int(contents[i])
		if index in vocab:
			word = vocab[index]
			freq = int(contents[i+iLen])
			for t in xrange(freq):
				newDocument.append(word)
		else:
			print "this is strange",fileLocation
	if len(newDocument)>0:
		newText = " ".join(newDocument)
		output = open("congress2007_clean/"+fileLocation.replace("documents/",""), "w")
		output.write(newText+"\n")
		output.close()

