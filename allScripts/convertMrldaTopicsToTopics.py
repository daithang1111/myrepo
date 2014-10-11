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
        print "usage: mrlda topic input, output file, num of words"
        sys.exit()

input_file = sys.argv[1]
output_file = sys.argv[2]
K = int(sys.argv[3])
streak = K+3
output =open(output_file,"w")
count =0
#read all text file from dir
with open(input_file) as docFile:
        for docLine in docFile:
		if count%streak>2:
			if count%streak ==streak-1:
				output.write(docLine.split()[0]+"\n")
			else:
				output.write(docLine.split()[0]+" ")
		count+=1
output.close()
