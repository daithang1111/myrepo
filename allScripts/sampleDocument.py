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

if len(sys.argv) < 4:
        print "usage: input dir, input file, percent, output file"
        sys.exit()

input_dir =sys.argv[1]
input_file = sys.argv[2]
percent = int(sys.argv[3])
output_file = sys.argv[4]

ratio = float(percent)/float(100)

output_mrlda =open(output_file+".mrlda","w")
output_mallet = open(output_file+".mallet","w")
output_stat = open(output_file+".stat","w")

#create dir for output_file

if not os.path.exists(output_file):
    os.makedirs(output_file)

#read all text file from dir
with open(input_file) as docFile:
       	for docLine in docFile:
		if ratio>random.random():
			filename = docLine.split("\t")[0]
			output_stat.write(filename+"\n")
			output_mrlda.write(docLine)
			output_mallet.write(filename+"\t"+docLine)
			#copy files so mallet command can be used
			if(os.path.isfile(input_dir+"/"+filename)):
                                shutil.copy(input_dir+"/"+filename, output_file)
output_stat.close()
output_mrlda.close()
output_mallet.close()
