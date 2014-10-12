import sys

if len(sys.argv) <4:
	print "Usage: topic file, mrlda doc-topic file, corpus file (timestamp actor document), algorithmName (mrlda)"
	sys.exit(0)

topicFile = sys.argv[1]
docTopicFile =sys.argv[2]
docFile = sys.argv[3]
algorithmName =sys.argv[4]

#read doc files and get mapping between index -data row
count=1
docMap =dict()
with open(docFile) as file:
	for line in file:
		docMap[count] = line.rstrip() #time actorid actorname docid title date text
		count +=1

#read topic
topicMap =dict()
count=1
output =open(algorithmName+".topic", 'w')

with open(topicFile) as file:
	for line in file:
		topicContent = line.rstrip()
		topicMap[count] = topicContent		
		output.write(algorithmName+str(count)+"\t"+topicContent+"\n")
		count+=1
output.close()

#write result of topic model
output = open(algorithmName+".topicresult",'w')

with open(docTopicFile) as file:
	for line in file:
		values = line.rstrip().split()
		row = docMap[int(values[0])] #the first item is doc id
		entries =row.split("\t")
		#get the highest prop topic for this document
		sumProp =0
		propValue=0
		indexValue=-1
		for i in xrange(len(values)-1):
			j=i+1
			tmp =float(values[j])
			sumProp+=tmp
			if propValue<tmp:
				indexValue =j
				propValue=tmp
		topicid = algorithmName + str(indexValue)

		tmpProp =str(propValue/float(sumProp))
		#other fields
		timestamp=entries[0]
		actorid = entries[1]
		docid=entries[3]

		output.write(actorid + "\t" + timestamp +"\t"+docid+"\t"+topicid+"\t"+ tmpProp+"\n")
output.close()
		

