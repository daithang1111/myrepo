#based on the original vocab, train corpus, create a new vocab and convert dev, test corpus according to new vocab
#with assumption that the name is corpus.dev, corpus.train, corpus.test where corpus can be any thing
import sys
import scipy.io
import numpy as np
import random

if len(sys.argv) < 2:
	print "usage: ldac corpus, vocab file"
	sys.exit()

###########################
def convert_new_vocab(line, map):
	xline =line.rstrip().split(' ')
	newLine=""
	newFreq=0
        for l in xline:
        	if l.find(':')!=-1:
                	i_f =l.split(':')
			index = int(i_f[0])
			if index in map:#this should be good
				newLine+=" "+str(map[index])+":"+i_f[1]
				newFreq+=1
	if newFreq >0:
		newLine =str(newFreq)+newLine+'\n'
        return newLine

#############################
corpus_file = sys.argv[1]
vocab_file = sys.argv[2]

#reading the original dictionaryvocab dict
vocab =dict()

index=0
with open(vocab_file,'r') as file:
	for line in file:
		vocab[index]=line.rstrip()
		index+=1

V =index #vocab size
print "Original Vocab Size:",V

vocab_train =dict()
#read train data, create dict for indices
with open(corpus_file+'.train', 'r') as file:
	for line in file:
		#create new indices
		xline =line.rstrip().split(' ')
		for l in xline:
			if l.find(':')!=-1:
				vocab_train[int(l.split(':')[0])]=True

#create mapping for new indices
vocab_file_train =open(vocab_file+'.train','w')
index=0
index_map_train=dict()
for i in xrange(V):
        if i in vocab_train:
                vocab_file_train.write(vocab[i]+'\n')
                index_map_train[i]=index
                index+=1

vocab_file_train.close()
print "vocab train size:",len(index_map_train)

#now, read dev, train, test and convert them to a new corpus based on new vocab (vocab.train)

#dev
output_dev = open(corpus_file+".dev.dev",'w')
with open(corpus_file+".dev", 'r') as file:
        for line in file:
		output_dev.write(convert_new_vocab(line,index_map_train))
output_dev.close()

#train
output_train = open(corpus_file+".train.train",'w')
with open(corpus_file+".train", 'r') as file:
        for line in file:
                output_train.write(convert_new_vocab(line,index_map_train))
output_train.close()


output_test = open(corpus_file+".test.test",'w')
with open(corpus_file+".test", 'r') as file:
        for line in file:
                output_test.write(convert_new_vocab(line,index_map_train))
output_test.close()

