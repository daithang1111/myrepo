#GRID search for anchor methods
#nips data contains 1500 docs (W), 12419 words (V)
#test anchor for seed=100,38, and random (R1)
#after figure out lambda, reapply model to test data
#INPUT: corpus file, vocab file, anchor dir, ldac dir, ti dir, specific fastRecover.py file

#OUTPUT: lambda

#STEPS:
#1. copies corpus file, vocab file, anchor dir, ldac dir, ti dir, settings.example (change parameters according to file size)

#2. convert corpus file to ldac format:
#python convert_docword_ldac.py docword.nips.txt nips.ldac

#3. create 15% data for development, 70% for train, and 15% for test
#python sampling.py nips.ldac #created nips.ldac.dev, nips.ldac.train, nips.ldac.test
#for 20newsdata, start from this step

#4. create a new vocab, and new corpora with format *.train.train/dev.dev/test.test
corpus=$1
F=$2
python build_new_vocab.py $corpus.ldac $corpus.vocab.txt

#5. create anchor file again for dev/train/test
python convert_ldacinput_anchorinput.py $corpus.ldac.dev.dev $corpus.anchor.dev
python convert_ldacinput_anchorinput.py $corpus.ldac.train.train $corpus.anchor.train
python convert_ldacinput_anchorinput.py $corpus.ldac.test.test $corpus.anchor.test

#6. truncate train vocab
python anchor-word-recovery/uci_to_scipy.py $corpus.anchor.train M_$corpus.train.mat
cd anchor-word-recovery
python truncate_vocabulary.py ../M_$corpus.train.mat ../$corpus.vocab.txt.train $F #for 20news
cd ..
#7. create new ldac corpus file based on dev, train,test
python convert_ldacdata_with_new_vocab.py $corpus.ldac.train.train $corpus.vocab.txt.train $corpus.vocab.txt.train.trunc #nips.ldac.train.train.new
python convert_ldacdata_with_new_vocab.py $corpus.ldac.dev.dev $corpus.vocab.txt.train $corpus.vocab.txt.train.trunc
python convert_ldacdata_with_new_vocab.py $corpus.ldac.test.test $corpus.vocab.txt.train $corpus.vocab.txt.train.trunc

#6. create new anchor corpus files
python convert_ldacinput_anchorinput.py $corpus.ldac.dev.dev.new $corpus.anchor.dev.new
python convert_ldacinput_anchorinput.py $corpus.ldac.train.train.new $corpus.anchor.train.new
python convert_ldacinput_anchorinput.py $corpus.ldac.test.test.new $corpus.anchor.test.new


#7. create ti corpus dirs for dev and test
rm -rf grid_corpus_*
mkdir grid_corpus_dev
mkdir grid_corpus_test
mkdir grid_corpus_train

python convert_docword_ti.py $corpus.anchor.dev.new $corpus.vocab.txt.train.trunc grid_corpus_dev/$corpus.ti.dev
python convert_docword_ti.py $corpus.anchor.test.new $corpus.vocab.txt.train.trunc grid_corpus_test/$corpus.ti.test
python convert_docword_ti.py $corpus.anchor.train.new $corpus.vocab.txt.train.trunc grid_corpus_train/$corpus.ti.train

cp grid_corpus_train/* grid_corpus_dev
cp grid_corpus_train/* grid_corpus_test
#8. create mat file to train anchor method
python anchor-word-recovery/uci_to_scipy.py $corpus.anchor.train.new M_$corpus.train.mat.trunc.mat

#9. create mallet files
python convert_to_mallet.py grid_corpus_dev/$corpus.ti.dev
python convert_to_mallet.py grid_corpus_test/$corpus.ti.test
python convert_to_mallet.py grid_corpus_train/$corpus.ti.train

#10 copy mallet files
mv grid_corpus_dev/$corpus.ti.dev.mallet .
mv grid_corpus_test/$corpus.ti.test.mallet .
mv grid_corpus_train/$corpus.ti.train.mallet .
