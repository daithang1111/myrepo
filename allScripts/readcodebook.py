import sys

if len(sys.argv)<3:
	print "Usage: codebook in text, vocab of corpus, prefix for informed"
	sys.exit()

docFile = sys.argv[1]
vocabFile = sys.argv[2]
prefix = sys.argv[3]


# get arbitrary 20 words
def getTopic(text,vocab):
        tmp =list()
        words =text.split()
        for word in words:
                if word in vocab:
                        if word not in tmp:
                                tmp.append(word)
        return " ".join(tmp[:20])

def getMrldaInform(text,vocab):
        tmp =list()
        words =text.split()
        for word in words:
		if word in vocab:
			if word not in tmp:
                		tmp.append(word)
        return " ".join(tmp)

def getSeededInform(text,vocab):
        tmp =list()
        words =text.split()
        for word in words:
                if word in vocab:
                        if word not in tmp:
                                tmp.append(word)
        return ",".join(tmp)




vocab = dict()
with open(vocabFile) as file:
	for line in file:
		word_index = line.rstrip().split("\t")
		vocab[word_index[0]]=1

d = dict()
with open(docFile) as file:
	for line in file:
		label_str = line.rstrip().split("\t")
		label =label_str[0]
		text = label_str[1]
		if label in d:
			d[label] =d[label]+" "+ text
		else:
			d[label]=text

outputMrlda =open(prefix+".mrlda.informed","w")
outputSeeded =open(prefix+".seededLDA.informed","w")
outputTopic =open(prefix+".topic","w")

for label in d:
	outputMrlda.write(getMrldaInform(d[label], vocab)+"\n")
	outputSeeded.write(getSeededInform(d[label],vocab)+"\n")
	outputTopic.write("codebook_"+label+"\t"+ getTopic(d[label], vocab)+"\n")

outputMrlda.close()
outputSeeded.close()
