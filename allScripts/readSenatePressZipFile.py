import zipfile

zf = zipfile.ZipFile("master.zip")
output = open("GrimmerSenatePressReleases.txt","w")
for filename in zf.namelist():
	if filename.endswith(".txt"):
		data = zf.read(filename)
		id = filename.replace("GrimmerSenatePressReleases-master/raw/","")
		output.write(id+"\t"+ data.replace("\t","")+"\n")
output.close()
		
