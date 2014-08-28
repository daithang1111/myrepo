if [ $# -ne 5 ]; then
        echo "We need 5 arguments: start_topic, end_topic, step, dev, vocab size"
        exit 0
fi

before="$(date +%s)"
metric="npmi"
K=$1
vSize=$5
devtest=$4
topwords=20
corpus=20news
while [ $K -le $2 ]
do
		
	#create .other file
        python create_other_mrlda.py $K $vSize mrlda.$K.other
		
        cd lda-c-dist
        ./lda inf inf-settings.txt ../mrlda.$K ../$corpus.ldac.$devtest.$devtest.new ../mrlda.$K.HL
        #calculate average likelihood
        python ../calculate_heldout_likelihood.py ../mrlda.$K.HL-lda-lhood.dat

	cd ..
	#compute the word occurrences
	#echo "Computing word occurrence..."
	#python topic_interpretability/ComputeWordCount.py repo/ti/topics/$loss.$corpus.train.$K.$rLambda.ti.topics raw_corpus_$devtest > repo/ti/wordcount/$loss.$corpus.train.$K.$rLambda.wc
	#compute the topic observed coherence
	#echo "Computing the observed coherence..."
	python topic_interpretability/ComputeObservedCoherence.py /cliphomes/daithang/TEST_WB/output/20news.mrlda.$K.topics.norm $metric /fs/clip-scratch/daithang/word_count/20news/20news_dev_raw.wc > /cliphomes/daithang/TEST_WB/output/20news.mrlda.$K.oc

	K=$(( $K + $3))
done
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo "Elapsed time for code block: $elapsed_seconds"

