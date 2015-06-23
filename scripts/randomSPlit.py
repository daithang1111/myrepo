import sys
import random
if len(sys.argv)<1:
	print "we need corpus prefix"
	sys.exit(0)

corpus = sys.argv[1]

textFile = corpus+".txt"
labelFile = corpus+".response"

trainFile = open(corpus+".train.txt",'w')
trainLabel=open(corpus+".train.response",'w')

testFile =open(corpus+".test.txt",'w')
testLabel=open(corpus+".test.response",'w')

p =0.2


t =list()
l = list()
with open(textFile) as file:
	for line in file:
		t.append(line.rstrip())
with open(labelFile) as file:
	for line in file:
		l.append(line.rstrip())

trainIndex=list()
testIndex=list()

for i in xrange(len(l)):
	if random.random()<p:
		testIndex.append(i)
	else:
		trainIndex.append(i)

for i in trainIndex:
	trainFile.write(t[i]+"\n")
	trainLabel.write(l[i]+"\n")

for i in testIndex:
	testFile.write(t[i]+"\n")
	testLabel.write(l[i]+"\n")

trainFile.close()
trainLabel.close()

testFile.close()
testLabel.close()












