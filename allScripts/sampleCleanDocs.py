#This script slip corpus into 15 70 15 percentage for DEV, TRAIN, TEST
import sys
import os
import glob
import math
import string
import random

#label files
if len(sys.argv)<2:
	print "how mcuh percent u would like to sample?"
	sys.exit(0)

ratio = float(sys.argv[1])/100

sample_labels = "congress2007.txt."+sys.argv[1]
sample_file = open(sample_labels, "w")


for fileLocation in glob.iglob(os.path.join('congress2007_clean','*')):
	r = random.random()
        if r<ratio:
		with open(fileLocation) as file:
			for line in file:
        			sample_file.write(fileLocation+"\t"+line)

sample_file.close()
