from sklearn.datasets import fetch_20newsgroups
from collections import Counter
from unidecode import unidecode
import cPickle, string
from sklearn.datasets.base import Bunch
import sys
def composePkl(textFile, labelFile):
	data=list()
	target=list()
	target_names=list()
	target_names.append("0")
	target_names.append("1")
	filenames=list()
	DESCR=list()
	with open(textFile) as file:
		for line in file:
			values =line.rstrip().split("\t")
			filenames.append(values[0])
			data.append(values[1])
	with open(labelFile) as file:
		for line in file:
			values =line.rstrip().split("\t")
			target.append(int(values[1]))
			#target_names.append(values[1])
	DESCR.append("NONE")
	return Bunch(DESCR=DESCR,data=data,target=target,target_names=target_names,filenames=filenames)

	



if len(sys.argv)<1:
	print "We need corpus prefix"
	sys.exit(0)

corpus = sys.argv[1]

trainFile =corpus+".train.txt"
trainLabel =corpus+".train.response"

trainBunch = composePkl(trainFile,trainLabel)

testFile =corpus+".test.txt"
testLabel =corpus+".test.response"

testBunch =composePkl(testFile,testLabel)

cPickle.dump((trainBunch, testBunch), open(corpus+'.pkl', 'wb'))


