echo "delete all generated dirs"
rm -rf repo_old anchor-word-recovery* topic_interpretability lda-c-dist*
mv repo repo_old
echo "create repo and its subdirs"
mkdir repo
mkdir repo/anchor
mkdir repo/anchor/output
mkdir repo/ldac
mkdir repo/ldac/corpus
mkdir repo/ldac/likelihood
mkdir repo/ti
mkdir repo/ti/corpus
mkdir repo/ti/topics
mkdir repo/ti/wordcount
mkdir repo/ti/results

echo "download LDAC"
wget http://www.cs.princeton.edu/~blei/lda-c/lda-c-dist.tgz
tar -xzvf lda-c-dist.tgz
cd lda-c-dist
echo "compile LDAC"
make
cd ..

#download topic_interpretability
echo "downloading topic_interpretability"
git clone https://github.com/jhlau/topic_interpretability

#download anchor-word-recovery project
echo "downloading anchor-word-recovery"
wget http://cs.nyu.edu/~halpern/files/anchor-word-recovery.zip
unzip anchor-word-recovery.zip
