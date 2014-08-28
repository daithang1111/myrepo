#Sample data from large c108 data
#Create data file for MrLDA
#Create data file for Mallet
import sys
import scipy.io
import numpy as np
import random
import re
import os
import glob
import math
import string
import shutil

if len(sys.argv) < 3:
        print "usage: word leng, doc size, num of docs"
        sys.exit()

letters = "a b c d e f g h i j k l m n o p q r s t x y z".split()

word_len = int(sys.argv[1])
doc_size = int(sys.argv[2])
num_doc = int(sys.argv[3])

def genLetter():
	index =int(random.random()*len(letters))
	return letters[index]

def genWord(len):
	word =""
	subLen = int(random.random()*len)+1
	for l in xrange(subLen):
		word+=genLetter()
	return word



for doc in xrange(num_doc):
	file_name = "doc."+str(doc)
	file_out = open(file_name,"w")
	for word in xrange(doc_size):
		w = genWord(word_len)
		file_out.write(w+" ")
	file_out.write("end\n")
	file_out.close()

