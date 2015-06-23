import sys

output =open("amazon.new",'w')
with open("amazon.response") as file:
	for line in file:
		values = line.rstrip().split("\t")
		label = float(values[1])
		if label >4:
			label ="1"
		else:
			label ="0"
		output.write(values[0]+"\t"+label+"\n")
output.close()
