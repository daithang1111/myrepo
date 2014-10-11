labelList = list()
with open("labels.txt") as file:
	for line in file:
		labelList.append(line.rstrip())


with open("predictions.out") as file:
	for line in file:
		bis = line.split("[")[1].split("]")[0].split(", ")
		labels =list()
		for i in xrange(len(bis)):
			if bis[i] =="true":
				labels.append(labelList[i])
		print "LABEL:",",".join(labels)
