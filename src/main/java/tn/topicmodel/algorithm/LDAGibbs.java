package tn.topicmodel.algorithm;

import tn.util.MathUtil;

public class LDAGibbs {
	int M = 10;
	int V = 10;
	int K = 10;
	double alpha = 0.1;
	double alphas[];

	double beta = 0.05;
	double betas[];
	int[][] docs;
	double thetas[][];
	double phis[][];
	int nmk[][];
	int nm[];
	int nkt[][];
	int nk[];
	int z[][];

	int MAX_ITERATIONS = 1000;

	int S = 100;

	/**
	 * 
	 * @param M
	 * @param V
	 * @param K
	 * @param docs
	 * @param alpha
	 * @param beta
	 */
	public LDAGibbs(int M, int V, int K, int[][] docs, double alpha, double beta,
			int max, int update) {
		this.M = M;
		this.V = V;
		this.K = K;
		this.docs = docs;
		this.alpha = alpha;

		this.beta = beta;
		// init alphas
		alphas = new double[K];
		for (int k = 0; k < K; k++) {
			alphas[k] = alpha;
			;
		}
		// betas
		betas = new double[V];
		for (int v = 0; v < V; v++) {
			betas[v] = beta;
		}
		this.MAX_ITERATIONS = max;
		this.S = update;
		thetas = new double[M][K];
		phis = new double[K][V];
		nmk = new int[M][K];
		nm = new int[M];
		nkt = new int[K][V];
		nk = new int[K];
		z = new int[M][];
	}

	/**
	 * 
	 */
	public void updateParams() {

		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				thetas[m][k] = (nmk[m][k] + alpha) / (nm[m] + K * alpha);
			}
		}
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++) {
				phis[k][w] = (nkt[k][w] + beta) / (nk[k] + V * beta);
			}
		}
	}

	public void run() {
		// init
		init();

		// gibbs
		for (int loop = 0; loop < MAX_ITERATIONS; loop++) {
			for (int m = 0; m < M; m++) {
				int Nm = docs[m].length;
				for (int n = 0; n < Nm; n++) {
					z[m][n] = sampleFullConditionals(m, n);
				}
			}

			if (loop % S == 0 && loop > 0) {
				updateParams();
				double likelihoodM = 0.0;
				for (int m = 0; m < M; m++) {
					try {
						likelihoodM += getLogLikelihood(m);

					} catch (Exception e) {
						System.out.println("ERROR:" + m);
					}
				}
				System.out.println("LOG-LIKELIHOOD:" + likelihoodM);

			}
		}

	}

	/**
	 * 
	 */
	public void init() {
		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				nmk[m][k] = 0;
			}
			nm[m] = 0;
		}

		for (int k = 0; k < K; k++) {
			for (int t = 0; t < V; t++) {
				nkt[k][t] = 0;
			}
			nk[k] = 0;
		}

		double uniform[] = new double[K];
		for (int i = 0; i < K; i++) {
			uniform[i] = (double) 1 / (double) K;
		}
		// init
		for (int m = 0; m < M; m++) {
			int Nm = docs[m].length;
			z[m] = new int[Nm];
			for (int n = 0; n < Nm; n++) {
				int k = MathUtil.drawMultinomial(K, uniform);
				z[m][n] = k;
				nmk[m][k] += 1;
				nm[m] += 1;
				nkt[k][docs[m][n]] += 1;
				nk[k] += 1;
			}
		}

	}

	/**
	 * 
	 * @param m
	 * @param n
	 * @return
	 */
	public int sampleFullConditionals(int m, int n) {
		int t = docs[m][n];
		int k = z[m][n];
		nmk[m][k]--;
		nm[m]--;
		nkt[k][t]--;
		nk[k]--;

		double zSamples[] = new double[K];
		for (int z = 0; z < K; z++) {
			zSamples[z] = ((nkt[z][t] + beta) / (nk[z] + V * beta))
					* ((nmk[m][z] + alpha) / (nm[m] + K * alpha));
		}

		int newK = MathUtil.drawMultinomial(K, zSamples);

		nmk[m][newK]++;
		nm[m]++;
		nkt[newK][t]++;
		nk[newK]++;

		return newK;
	}

	public double getLogLikelihood(int m) {
		double likelihood = 0.0;
		for (int n = 0; n < docs[m].length; n++) {

			int w = docs[m][n];
			double sum = 0.0;
			for (int k = 0; k < K; k++) {
				sum += phis[k][w] * thetas[m][k];
			}
			likelihood += Math.log(sum);
		}
		return likelihood;
	}

	public double[][] getThetas() {
		return thetas;
	}

	public void setThetas(double[][] thetas) {
		this.thetas = thetas;
	}

	public double[][] getPhis() {
		return phis;
	}

	public void setPhis(double[][] phis) {
		this.phis = phis;
	}

	public int[][] getZ() {
		return z;
	}

	public void setZ(int[][] z) {
		this.z = z;
	}

}
