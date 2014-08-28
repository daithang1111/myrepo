if [ $# -ne 10 ]; then
        echo "We need 10 arguments: start_topic, end_topic, step, start lambda, end lambda, step (multiply by 1000), corpus, regularization option (Beta or L2), dev or test, M number"
        exit 0
fi

before="$(date +%s)"
metric="npmi"
loss="KL"
K=$1
corpus=$7
REG=$8
devtest=$9
M=${10}
topwords=20
while [ $K -le $2 ]
do
	rLambda=$4
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
		
		cd ..
		rLambda=$(( $rLambda + $6))
	done
	K=$(( $K + $3))
done
after="$(date +%s)"
elapsed_seconds="$(expr $after - $before)"
echo "Elapsed time for code block: $elapsed_seconds"

