load('SparseDocs.RData')  ##the press release data, as sparse matrix
load('WordsUsed.RData')   ##the words that are used 


##setting my length command
len<- length

#print out the words
write(words2,file="words.tn")

#print out the names of senators
names<- c()
for(j in 1:3){
        names<- c(names, names(list.press[[j]]))
                }
names.people<- names

write(names, file="senators.tn")

#print out documents
docDir ="documents/"
years =c(2005,2006,2007)
for(j in 1:3){
        press = list.press[[j]]
	senatorNames = names(press)
        for(k in 1:len(press)){
                senator = press[[k]]
		senatorName =senatorNames[k]
		docNames =names(senator) 
		for (z in 1:len(senator)){
			tmpOutFile = paste(docDir,years[j],".",senatorName,".",gsub("\\\\",".",docNames[z]),sep="")
			write(senator[[z]], file=tmpOutFile, ncolumns=1)	
		}
		
        }
}

