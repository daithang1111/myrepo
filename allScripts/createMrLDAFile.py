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

if len(sys.argv) < 2:
        print "usage: warcbase text dir, output file"
        sys.exit()

text_dir = sys.argv[1]
output_file = sys.argv[2]

pattern = re.compile('[\W_]+')

output =open(output_file,"w")
#read all text file from dir
for fileLocation in glob.iglob(os.path.join(text_dir,'*')):
	id = fileLocation.split("/")[1]
	document=""
	with open(fileLocation) as docFile:
        	for docLine in docFile:
			document+=" "+docLine.rstrip()
	tmp =re.sub(pattern,' ',document)
	output.write(id+"\t"+tmp+"\n")

output.close()
