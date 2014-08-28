K=100
step=100
MAX=900
while [ $K -le $MAX ]
do
	cp -p  settings.example.$K settings.example
	sh runRepeat.sh 3 run_full.sh O_M_$K 20 80 20 1 1 1 nips original dev $K
	K=$(( $K + $step))
done

