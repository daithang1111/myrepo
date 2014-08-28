if [ $# -ne 9 ]; then
        echo "We need 9 arguments: start_topic, end_topic, step, start lambda, end lambda, step (multiply by 1000), corpus, regularization option (Beta or L2), dev or test"
        exit 0
fi

before="$(date +%s)"
metric="npmi"
loss="gurobi_L2"
K=$1
corpus=$7
REG=$8
devtest=$9
topwords=20
M=700
while [ $K -le $2 ]
do
	if [ $K -eq 60 ]; then
		M=800
	fi
	if [ $K -eq 80 ]; then
		M=600
	fi
	
	echo "Value of M is :$M"
	rLambda=$4
	STEP=$6
	while [ $rLambda -le $5 ]
	do
		tmpLambda=$rLambda/1000
		python embed_lambda_into_file.py fastRecover_$REG.py $tmpLambda
		mv fastRecover_$REG.py.tmp fastRecover_$REG.py
		cp fastRecover_$REG.py anchor-word-recovery/fastRecover.py
		cd anchor-word-recovery
		echo "learning with nonnegative recover method using KL loss"
		python learn_topics.py ../M_$corpus.train.mat.trunc.mat ../settings.example.$M ../$corpus.vocab.txt.train.trunc $K $loss $loss.out.$corpus.train.$K.$rLambda
       
		#convert A to beta
		cp $loss.out.$corpus.train.$K.$rLambda.A ../repo/anchor/output
                python ../convert_A_to_beta.py $K $loss.out.$corpus.train.$K.$rLambda.A ../repo/anchor/output/$loss.out.$corpus.train.$K.$rLambda.beta
		
		#create .other file
                python ../create_other_ldac.py $K $loss.out.$corpus.train.$K.$rLambda.A ../repo/anchor/output/$loss.out.$corpus.train.$K.$rLambda.other
		
		#get topic list
                python ../lda-c-dist/topics.py ../repo/anchor/output/$loss.out.$corpus.train.$K.$rLambda.beta ../$corpus.vocab.txt.train.trunc $topwords >$loss.$corpus.train.$K.$rLambda.ldac.topics.tmp
                python ../convert_ldacout_titopics.py $topwords $loss.$corpus.train.$K.$rLambda.ldac.topics.tmp ../repo/anchor/output/$loss.$corpus.train.$K.$rLambda.ldac.topics
		
		#create topic file for ti
		cut -d':' -f2 $loss.out.$corpus.train.$K.$rLambda.topwords |cut -c2- >../repo/anchor/output/$loss.$corpus.train.$K.$rLambda.anchor.topics
		cp $loss.out.$corpus.train.$K.$rLambda.topwords ../repo/anchor/output

		cp ../repo/anchor/output/$loss.$corpus.train.$K.$rLambda.anchor.topics ../repo/ti/topics/$loss.$corpus.train.$K.$rLambda.ti.topics
		#cut -d':' -f2 $loss.out.$corpus.train.$K.$rLambda.topwords |cut -c2- >../$loss.$corpus.train.$K.$rLambda.ti.topics
		
		#compare list of topics
                #diff ../repo/anchor/output/$loss.$corpus.train.$K.$rLambda.ldac.topics ../repo/anchor/output/$loss.$corpus.train.$K.$rLambda.anchor.topics >../repo/anchor/output/$loss.$corpus.$K.$rLambda.topic.diff
		
		#run Inf on LDAC test, since there is no alpha, set alpha =1/K
                #cd ../lda-c-dist
                #./lda inf inf-settings.txt ../repo/anchor/output/$loss.out.$corpus.train.$K.$rLambda ../$corpus.ldac.$devtest.$devtest.new ../repo/ldac/likelihood/$corpus.$K.$rLambda.HL
                #calculate average likelihood
                #python ../calculate_heldout_likelihood.py ../repo/ldac/likelihood/$corpus.$K.$rLambda.HL-lda-lhood.dat

		cd ..
		#compute the word occurrences
		#echo "Computing word occurrence..."
		#python topic_interpretability/ComputeWordCount.py repo/ti/topics/$loss.$corpus.train.$K.$rLambda.ti.topics raw_corpus_$devtest > repo/ti/wordcount/$loss.$corpus.train.$K.$rLambda.wc
		#compute the topic observed coherence
		#echo "Computing the observed coherence..."
		#python topic_interpretability/ComputeObservedCoherence.py repo/ti/topics/$loss.$corpus.train.$K.$rLambda.ti.topics $metric repo/ti/wordcount/$loss.$corpus.train.$K.$rLambda.wc > repo/ti/results/$loss.$corpus.train.$K.$rLambda.oc

		if [ $rLambda -eq 100 ]; then
                        STEP=100 #we need to jump from 10->90 to 100->1000 with step =100
                fi
		rLambda=$(( $rLambda + $STEP))
	done
	K=$(( $K + $3))
done
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo "Elapsed time for code block: $elapsed_seconds"
