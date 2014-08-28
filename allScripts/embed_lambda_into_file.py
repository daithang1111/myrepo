import sys
import scipy.io
import numpy as np

if len(sys.argv) < 2:
	print "usage: fastRecover file, lambda"
	sys.exit()

fastRecoverFile = sys.argv[1]
rLambda = sys.argv[2].split("/")[0]
print rLambda
newLambda =float(rLambda)/1000
print newLambda
output = open(fastRecoverFile+'.tmp', 'w')

with open(fastRecoverFile, 'r') as file:
	for line in file:
		if line.find("rLambda =")==-1:
			 output.write(line)
		else:
			output.write("\trLambda ="+str(newLambda)+"\n")
output.close()
