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
cp -rf /cliphomes/daithang/CAMERA_READY_2014/SOFTWARE/topic_interpretability .
cp -rf /cliphomes/daithang/CAMERA_READY_2014/SOFTWARE/lda-c-dist .
cp -rf /cliphomes/daithang/CAMERA_READY_2014/SOFTWARE/anchor-word-recovery .
