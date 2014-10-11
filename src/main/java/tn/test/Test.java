package tn.test;

import tn.topicmodel.algorithm.LDAGenerative;
import tn.topicmodel.algorithm.LDAGibbs;

public class Test {

	public static void main(String[] args) {
		LDAGenerative ldaGen = new LDAGenerative(10, 20, 50, 10, 0.01, 0.5);
		ldaGen.generate();
		// print sample docs
//		System.out.println("Before sampling:");
//		ldaGen.print();

		LDAGibbs ldaGibbs = new LDAGibbs(ldaGen.getM(), ldaGen.getV(),
				ldaGen.getK(), ldaGen.getDocs(), ldaGen.getAlpha(),
				ldaGen.getBeta(), 20000, 100);
		
		ldaGibbs.run();
		//print draw
		ldaGen.setZ(ldaGibbs.getZ());

		// System.out.println("After sampling:");
		// ldaGen.print();
		
	}

}
