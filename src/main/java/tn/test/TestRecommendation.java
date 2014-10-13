package tn.test;

import java.util.Arrays;
import java.util.List;

import tn.model.generic.Document;
import tn.model.recommendation.MatchedDocument;
import tn.recommendation.CorpusUtility;
import tn.recommendation.Main;
import tn.recommendation.RecUtil;

public class TestRecommendation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// test jaccard
		List<Document> corpus = RecUtil.corpus;

		CorpusUtility util = new CorpusUtility(corpus);
		/*
		 * if (Consts.debug) System.out.println(util.getVocab().getWord(10));
		 * 
		 * Document doc1 = corpus.get(0); Document doc2 = corpus.get(1); if
		 * (Consts.debug) { System.out.println(doc1.toString());
		 * System.out.println(doc2.toString()); } if (Consts.debug)
		 * System.out.println("Jaccard score:" + util.computeJaccardScore(doc1,
		 * doc2));
		 * 
		 * // test matched document MatchedDocument[] matches = new
		 * MatchedDocument[corpus.size()]; for (int i = 0; i < corpus.size();
		 * i++) { MatchedDocument md = new MatchedDocument(corpus.get(i),
		 * Math.random()); matches[i] = md; }
		 * 
		 * Arrays.sort(matches);
		 * 
		 * for (int i = 0; i < matches.length; i++) { if (Consts.debug)
		 * System.out.println(matches[i].getDoc().getDate() + ":" +
		 * matches[i].getScore()); }
		 */
		// test similarity based on docid
		String docid = "docid0";
		System.out.println("TESTING FOR DOCUMENT: docid0");

		Main main = new Main();
		MatchedDocument[] matches = main
				.getDocumentsBasedOnDocumentSimilarity(docid);
		if (matches != null && matches.length > 0) {
			for (int i = 0; i < matches.length; i++) {

				System.out.println(matches[i].getDoc().getDate() + "\t"
						+ matches[i].getScore() + "\t"
						+ matches[i].getDoc().toString());
			}
		}
		// test similarity based on userid
		String userid = "userid0";
		System.out.println("\n\nTESTING FOR USER: userid0");

		matches = main.getDocumentsBasedOnUserSimilarity(userid);
		if (matches != null && matches.length > 0) {
			for (int i = 0; i < matches.length; i++) {

				System.out.println(matches[i].getDoc().getDate() + "\t"
						+ matches[i].getScore() + "\t"
						+ matches[i].getDoc().toString());
			}
		}

	}

}
