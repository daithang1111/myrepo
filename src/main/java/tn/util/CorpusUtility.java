package tn.util;

/**
 * @author nguyentd4
 * 
 * 
 * Assumption is that we only work with one corpus of English articles D
 * An user read an article, then the value of score[userid][docid] = 1, 0 otherwise
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import tn.model.generic.Document;
import tn.model.generic.Vocabulary;
import tn.model.recommendation.SparseVector;
import tn.recommendation.RecUtil;

public class CorpusUtility {

	/**
	 * properties
	 */

	private Vocabulary vocab;
	private List<Document> corpus;

	private int vocabSize = 0;

	/**
	 * constructor
	 * 
	 * @param corpus
	 */
	public CorpusUtility(List<Document> corpus) {
		this.corpus = new ArrayList<Document>(corpus);
		if (RecUtil.debug)
			System.out.println(corpus.size());
		this.buildVocabulary();
		vocabSize = vocab.getVocabSize();
	}

	public Vocabulary getVocab() {
		return vocab;
	}

	/**
	 * compute jaccard score between 2 documents first convert each documents to
	 * a sparsevector of important terms based on TF-IDF scores then get jaccard
	 * score based on two lists of words
	 * 
	 * @param doc1
	 * @param doc2
	 * @return
	 */

	public double computeJaccardScore(Document doc1, Document doc2) {
		if (RecUtil.debug)
			System.out.println("Computing Jaccard score for ("
					+ doc1.getDocTitle() + "," + doc2.getDocTitle() + ")");
		return RecUtil.getJaccardScoreForIndexSets(getTFIDFSparseVector(doc1)
				.getIndexSet(), getTFIDFSparseVector(doc2).getIndexSet());
	}

	/**
	 * 
	 * @param doc1
	 * @param doc2
	 * @return
	 */
	public double computeCosineScore(Document doc1, Document doc2) {
		return RecUtil.getCosineScore(getTFIDFSparseVector(doc1),
				getTFIDFSparseVector(doc2));
	}

	/**
	 * convert a document into sparsevector of important terms first create a
	 * sparse vector of word-frequency then compute tfidf score for each word,
	 * cutoff by a threshold NOTE: the value here is 1, but it can be frequency
	 * or tfidf, depending on what similarity measure get used
	 * 
	 * @param doc
	 * @return
	 */
	public SparseVector getTFIDFSparseVector(Document doc) {
		SparseVector sv = getFrequencySparseVector(doc);
		if (RecUtil.debug)
			System.out.println("FREQ SPARSE VECTOR for " + doc.getDocTitle()
					+ ":" + sv.toString());
		SparseVector output = new SparseVector(vocabSize);

		Set<Entry<Integer, Double>> set = sv.getMap().entrySet();
		for (Entry<Integer, Double> entry : set) {
			double tfidf = getTFIDF(entry.getKey(), sv);
			if (tfidf >= RecUtil.TFIDF_THRESHOLD) {
				if (RecUtil.debug)
					System.out.println("adding "
							+ this.vocab.getWord(entry.getKey())
							+ " to sparse vector for document "
							+ doc.getDocTitle());
				output.set(entry.getKey(), 1);// entry.getValue()) or tfidf can
												// be
												// used here in
												// general case
			}
		}
		return output;
	}

	/**
	 * convert document into word:frequency sparse vector assume word in
	 * lowercase
	 * 
	 * @param doc
	 * @return
	 */
	private SparseVector getFrequencySparseVector(Document doc) {
		if (RecUtil.debug)
			System.out.println("creating frq sparse vector for "
					+ doc.getDocTitle() + ", text is " + doc.getDocContent());
		StringTokenizer st = new StringTokenizer(doc.getDocContent());
		List<String> words = new ArrayList<String>();
		while (st.hasMoreElements()) {
			words.add((String) st.nextElement());
		}
		if (RecUtil.debug)
			System.out.println("original doc len = " + words.size());
		// remove stopwords
		words.removeAll(RecUtil.stop_words);

		if (RecUtil.debug)
			System.out.println("after removing stopwords, len = "
					+ words.size());

		// create sparsevector
		SparseVector sv = new SparseVector(vocabSize);
		for (String w : words) {
			sv.increment(vocab.getIndex(w), 1);
		}

		return sv;
	}

	/**
	 * build vocabulary from corpus
	 * 
	 * @param corpus
	 */
	private final void buildVocabulary() {
		vocab = new Vocabulary();
		for (Document d : corpus) {
			vocab.addText(d.getDocContent());
		}
	}

	/**
	 * 
	 * @param wordIndex
	 * @param sv
	 * @return
	 */
	private double getTFIDF(int wordIndex, SparseVector sv) {
		return getTF(wordIndex, sv) * getIDF(wordIndex);
	}

	/**
	 * get augmented TF
	 * 
	 * @param wordIndex
	 * @param sv
	 * @return
	 */
	private double getTF(int wordIndex, SparseVector sv) {

		return 0.5 + (0.5 * sv.get(wordIndex) / (double) sv.getMaxValue());

	}

	/**
	 * 
	 * @param wordIndex
	 * @return
	 */
	private double getIDF(int wordIndex) {
		return Math.log((double) getTotalDoc()
				/ ((double) getDF(wordIndex) + 1));
	}

	/**
	 * TODO: implement get document frequency suppose that we have an inverted
	 * index of word ->documents then it's easy to get the number of documents
	 * that contain a word this function can be in mapreduce if needed
	 * 
	 * @param wordIndex
	 * @return
	 */
	private int getDF(int wordIndex) {
		return 10;
	}

	/**
	 * 
	 * @return
	 */
	private int getTotalDoc() {
		return this.corpus.size();
	}

}
