package tn.util;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.special.Gamma;

public class MathUtil {

	public static void main(String[] args){
		//test 
		int K=10;
		double[] alphas=new double[K];
		for(int k=0;k<K;k++){
			alphas[k] =0.1;
		}
		
		System.out.println(logBetaFunction(K, alphas));
	}
	public static double logBetaFunction(int K, double[] alphas) {
		if (K < 1 || alphas.length != K) {
			System.out.println("Inputs are not matched");
			return -1;
		}

		double sum = 0.0;
		double output = 0.0;
		for (int i = 0; i < K; i++) {
			sum += alphas[i];
			output += Math.log(Gamma.gamma(alphas[i]));
		}
		output -= Math.log(Gamma.gamma(sum));
		return output;
	}

	public static int drawMultinomial(int K, double[] inputP) {
		if (K < 1 || inputP.length != K) {
			System.out.println("Inputs are not matched");
			return -1;
		}

		double p[] = new double[K];
		for (int i = 0; i < K; i++) {
			p[i] = inputP[i];
		}
		for (int i = 1; i < K; i++) {
			p[i] += p[i - 1];
		}
		double u = Math.random() * p[K - 1];
		int selected = 0;
		for (int i = 0; i < K; i++) {
			if (u < p[i]) {
				selected = i;
				break;
			}
		}
		return selected;

	}

	public static double[] drawDirichlet(int K, double[] alphas) {
		if (K < 1 || alphas.length != K) {
			System.out.println("Length doesn't match");
			return null;
		}

		double[] ys = new double[K];
		double sumYs = 0.0;
		for (int i = 0; i < K; i++) {
			GammaDistribution gd = new GammaDistribution(alphas[i], 1);
			ys[i] = gd.sample();
			sumYs += ys[i];
		}
		if (sumYs <= 0) {
			System.out.println("Please check alphas");
			return null;
		}
		for (int i = 0; i < K; i++) {
			ys[i] /= sumYs;
		}
		return ys;
	}

	public static int generateDocLength(int mean) {
		PoissonDistribution pd = new PoissonDistribution(mean);
		return pd.sample();
	}

	public static double computeThetaDiff(double[][] thetas,
			double[][] testThetas, int M, int K) {
		double sum = 0.0;
		for (int m = 0; m < M; m++) {
			sum += computeVectorDiff(thetas[m], testThetas[m], K);
		}
		return sum;
	}

	public static double computeVectorDiff(double[] vec1, double[] vec2, int len) {
		double sum = 0.0;
		for (int i = 0; i < len; i++) {
			double s = vec1[i] - vec2[i];
			sum += s * s;
		}
		return Math.sqrt(sum);

	}
}
