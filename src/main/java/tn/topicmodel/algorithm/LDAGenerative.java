package tn.topicmodel.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import tn.recommendation.Vocabulary;
import tn.util.MathUtil;

public class LDAGenerative {
	int M = 10; // number of document
	int K = 3; // number of topics
	int avgLength = 10;
	int V = 10;// number of words
	double alpha;
	double beta;
	double betas[];
	double alphas[];
	double phis[][];
	double thetas[][];
	int docs[][];
	int z[][];
	Vocabulary topicVocab;
	Vocabulary wordVocab;

	/**
	 * Constructor
	 * 
	 * @param M
	 * @param K
	 * @param V
	 * @param avgLength
	 * @param alpha
	 * @param beta
	 */
	public LDAGenerative(int M, int K, int V, int avgLength, double alpha, double beta) {
		this.M = M;
		this.K = K;
		this.V = V;
		this.avgLength = avgLength;
		this.alpha = alpha;
		this.beta = beta;
		// topic vocab
		topicVocab = new Vocabulary();
		wordVocab = new Vocabulary();
		// init alphas
		alphas = new double[K];
		for (int k = 0; k < K; k++) {
			alphas[k] = alpha;
			topicVocab.addWord("t" + (k+1));
		}
		// betas
		betas = new double[V];
		for (int v = 0; v < V; v++) {
			betas[v] = beta;
			wordVocab.addWord("w" + (v+1));
		}
		// phis
		phis = new double[K][V];
		thetas = new double[M][K];
		// docs
		docs = new int[M][];
		z = new int[M][];
	}

	/**
	 * Generate document according to LDA generative process
	 */
	public void generate() {

		for (int i = 0; i < K; i++) {
			phis[i] = MathUtil.drawDirichlet(V, betas);
		}

		// thetas
		for (int m = 0; m < M; m++) {
			thetas[m] = MathUtil.drawDirichlet(K, alphas);
			int Nm = MathUtil.generateDocLength(avgLength);
			docs[m] = new int[Nm];
			z[m] = new int[Nm];
			for (int n = 0; n < Nm; n++) {
				int zmn = MathUtil.drawMultinomial(K, thetas[m]);
				int wmn = MathUtil.drawMultinomial(V, phis[zmn]);
				docs[m][n] = wmn;
				z[m][n] = zmn;
			}
		}
	}

	public void print() {

		for (int m = 0; m < M; m++) {
			int Nm = docs[m].length;
			List<String> doc = new ArrayList<String>();
			for (int n = 0; n < Nm; n++) {
				String word = wordVocab.getWord(docs[m][n] + 1);
				String topic = topicVocab.getWord(z[m][n] + 1);
				doc.add(word + "(" + topic + ")");
			}

			System.out.println(Joiner.on(" ").join(doc));
		}

	}

	/*** getter/setter ***/
	public int getM() {
		return M;
	}

	public void setM(int m) {
		M = m;
	}

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
	}

	public int getAvgLength() {
		return avgLength;
	}

	public void setAvgLength(int avgLength) {
		this.avgLength = avgLength;
	}

	public int getV() {
		return V;
	}

	public void setV(int v) {
		V = v;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}

	public double[] getBetas() {
		return betas;
	}

	public void setBetas(double[] betas) {
		this.betas = betas;
	}

	public double[] getAlphas() {
		return alphas;
	}

	public void setAlphas(double[] alphas) {
		this.alphas = alphas;
	}

	public double[][] getPhis() {
		return phis;
	}

	public void setPhis(double[][] phis) {
		this.phis = phis;
	}

	public double[][] getThetas() {
		return thetas;
	}

	public void setThetas(double[][] thetas) {
		this.thetas = thetas;
	}

	public int[][] getDocs() {
		return docs;
	}

	public void setDocs(int[][] docs) {
		this.docs = docs;
	}

	public int[][] getZ() {
		return z;
	}

	public void setZ(int[][] z) {
		this.z = z;
	}

	public Vocabulary getTopicVocab() {
		return topicVocab;
	}

	public void setTopicVocab(Vocabulary topicVocab) {
		this.topicVocab = topicVocab;
	}

	public Vocabulary getWordVocab() {
		return wordVocab;
	}

	public void setWordVocab(Vocabulary wordVocab) {
		this.wordVocab = wordVocab;
	}

}
