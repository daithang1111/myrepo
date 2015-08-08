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

devFile = open(corpus+".dev.txt",'w')
devLabel=open(corpus+".dev.response",'w')

testFile =open(corpus+".test.txt",'w')
testLabel=open(corpus+".test.response",'w')

p =0.1


t =list()
l = list()
with open(textFile) as file:
	for line in file:
		t.append(line.rstrip())
with open(labelFile) as file:
	for line in file:
		l.append(line.rstrip())

trainIndex=list()
devIndex=list()
testIndex=list()

for i in xrange(len(l)):
	rand =random.random()
	if rand<p:
		devIndex.append(i)
	elif rand>=p and rand<2*p:
		testIndex.append(i) 
	else:
		trainIndex.append(i)

for i in trainIndex:
	trainFile.write(t[i]+"\n")
	trainLabel.write(l[i]+"\n")

for i in devIndex:
        devFile.write(t[i]+"\n")
        devLabel.write(l[i]+"\n")

for i in testIndex:
	testFile.write(t[i]+"\n")
	testLabel.write(l[i]+"\n")

trainFile.close()
trainLabel.close()

devFile.close()
devLabel.close()

testFile.close()
testLabel.close()












