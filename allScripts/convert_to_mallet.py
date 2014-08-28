import sys
import scipy.io
import numpy as np

if len(sys.argv) < 1:
	print "usage: ti_file"
	sys.exit()

print "convert files to mallet format"
ti_file = sys.argv[1]
mallet_file=ti_file+".mallet"

numdocs=1
output = open(mallet_file,'w')
with open(ti_file, 'r') as file:
	for line in file:
		output.write("doc"+str(numdocs)+"\tNOINFO\t"+line)
		numdocs+=1
output.close()	
