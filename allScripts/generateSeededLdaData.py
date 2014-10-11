import sys
import os

def getDocForm(text, indexOfDoc):
	words = text.split()
	output = list()
	for word in words:
		output.append(str(indexOfDoc))
	return "\n".join(output)

def getWordForm(text, vocab):
	words = text.split()
	output =list()
	for word in words:
		tmpIndex = vocab[word]
		output.append(tmpIndex)
	return "\n".join(output)


if len(sys.argv)<3:
	print "Usage: vocabulary, mrlda documents, output prefix for seeded lda (seededLDA)"
	sys.exit()

vocabFile = sys.argv[1]
docFile = sys.argv[2]
outputPrefix=sys.argv[3]
#vocab
vocab = dict()
inverseVocab=dict()
with open(vocabFile) as file:
	for line in file:
		word_index = line.rstrip().split("\t")
		vocab[word_index[0]] = word_index[1]
		inverseVocab[word_index[1]]=word_index[0]

vocabOut = outputPrefix+".vocab"
outputWriter =open(vocabOut,'w')
for index in sorted(inverseVocab.iterkeys()):
	outputWriter.write(inverseVocab[index]+"\n")

outputWriter.close()

#doc
docOut =outputPrefix+".doc"
docWriter =open(docOut,'w')

wordOut = outputPrefix+".word"
wordWriter =open(wordOut,'w')

docIndex=1
with open(docFile) as file:
	for line in file:
		label_text=line.rstrip().split("\t")
		docWriter.write(getDocForm(label_text[1],docIndex)+"\n")
		wordWriter.write(getWordForm(label_text[1],vocab)+"\n")
		docIndex+=1
docWriter.close()
wordWriter.close()				

