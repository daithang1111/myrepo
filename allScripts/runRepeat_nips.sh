if [ $# -ne 12 ]; then
        echo "We need 12 arguments: iterations, script name, alias name,  start_topic, end_topic, step, start lambda, end lambda, step (multiply by 1000), corpus, regularization, dev or test"
        exit 0
fi


echo "run $1 times this script $2 with random seed under the alias name $3 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12}"

L=1
while [ $L -le $1 ]
do
	echo "RUN $L"
	sh copy_softwares.sh
	#sh clear_and_rebuild.sh
	sh $2 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12}
	mv repo repo_$3_$L
	L=$(($L + 1))
done

