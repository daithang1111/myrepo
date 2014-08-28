if [ $# -ne 13 ]; then
        echo "We need 13 arguments: iterations, script name, alias name,  start_topic, end_topic, step, start lambda, end lambda, step (multiply by 1000), corpus, regularization, dev or test, max M number (will run 100->M, step 100) "
        exit 0
fi


echo "run $1 times this script $2 with random seed under the alias name $3 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12} ${13}"

M=${13}
echo "The value of M is $M"
L=1
while [ $L -le $1 ]
do
	echo "RUN $L, $M"
	sh copy_softwares.sh
	sh $2 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12} $M
	mv repo repo_$3_$L
	L=$(($L + 1))
done

