package tn.recommendation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import tn.model.generic.Document;
import tn.model.recommendation.SparseVector;
import tn.util.CorpusUtility;

public final class RecUtil {
	public static final String stopWordFileName = null;
	public static final double TFIDF_THRESHOLD = 0.1;
	public static final double USER_SIMILARITY_THRESHOLD = 0.1;
	public static final double DOC_SIMILARITY_THRESHOLD = 0.1;
	public static final int CUTOFF_FREQ = 5;
	public static List<String> stop_words;

	public static final int NUM_USERS = 5;
	public static final int NUM_DOCS = 100;
	public static final int LEN_DOC = 10;
	public static final int NUM_DATE = 10;

	public static final boolean debug = false;

	public static final List<Document> corpus = RecUtil.genRandomCorpus();
	public static final List<String> users = RecUtil.genRandomUsers();

	static {
		try {
			stop_words = new ArrayList<String>();
			if (stopWordFileName != null) {
				BufferedReader reader = new BufferedReader(new FileReader(
						stopWordFileName));
				String line = null;
				while ((line = reader.readLine()) != null) {
					stop_words.add(line.toLowerCase());
				}
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static double getJaccardScoreForIndexSets(Set<Integer> set1,
			Set<Integer> set2) {
		if (!set1.isEmpty() && !set2.isEmpty()) {
			if (RecUtil.debug)
				System.out.println("Two sets are non-empty");
			Set<Integer> intersection = new HashSet<Integer>(set1);
			intersection.retainAll(set2);

			Set<Integer> union = new HashSet<Integer>(set1);
			union.addAll(set2);

			return (double) intersection.size() / (double) union.size();
		} else {
			if (RecUtil.debug)
				System.out.println("One/or both sets are empty");
		}
		return 0;
	}

	/**
	 * 
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static double getJaccardScoreForWordSets(Set<String> set1,
			Set<String> set2) {
		if (!set1.isEmpty() && !set2.isEmpty()) {
			Set<String> intersection = new HashSet<String>(set1);
			intersection.retainAll(set2);

			Set<String> union = new HashSet<String>(set1);
			union.addAll(set2);

			return (double) intersection.size() / (double) union.size();
		}
		return 0;
	}

	/**
	 * 
	 * @param sv1
	 * @param sv2
	 * @return
	 */
	public static double getCosineScore(SparseVector sv1, SparseVector sv2) {
		// TODO: implement cosine similarity
		return 0.0;
	}

	/**
	 * @TODO: get document from database
	 * @param docid
	 * @return
	 */
	public static Document getDocument(String docid) {

		if (isValid(docid) && corpus != null) {
			if (RecUtil.debug)
				System.out.println("Getting document for docid= " + docid);
			for (int i = 0; i < corpus.size(); i++) {
				Document doc = corpus.get(i);
				if (doc.getDocId().equalsIgnoreCase(docid)) {
					return doc;
				}
			}
		}
		return null;
	}

	/**
	 * check if a string id empty
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isValid(String str) {
		return (str != null) && (!str.isEmpty());
	}

	/**
	 * TODO: get all documents that user has read so far
	 * 
	 * @param userid
	 * @return
	 */
	public static List<Document> getReadDocuments(String userid) {
		if (corpus != null) {
			int len = (int) (Math.random() * corpus.size() + 1);
			List<Document> readDocs = new ArrayList<Document>();
			for (int i = 0; i < len; i++) {
				readDocs.add(corpus.get((int) (Math.random() * corpus.size())));
			}
			return readDocs;
		}
		return null;
	}

	/**
	 * TODO: get recent documents (not too far in time)
	 * 
	 * @return
	 */
	public static List<Document> getRecentDocuments() {
		if (RecUtil.debug)
			System.out.println("Getting recent documents from database");
		return corpus;
	}

	/**
	 * TODO: get all userids of users who are reading articles
	 * 
	 * @return
	 */
	public static List<String> getReadingUsers() {
		if (users != null) {
			int len = (int) (Math.random() * users.size() + 1);
			List<String> readingUsers = new ArrayList<String>();
			for (int i = 0; i < len; i++) {
				readingUsers
						.add(users.get((int) (Math.random() * users.size())));

			}
			return readingUsers;

		}
		return null;
	}

	/**
	 * TODO: get document currently read by this user //assume just 1 document
	 * 
	 * @param userid
	 * @return
	 */
	public static Document getReadingDocument(String userid) {
		if (corpus != null) {
			return corpus.get((int) (Math.random() * corpus.size()));
		}
		return null;
	}

	/**
	 * for each user, get all documents which have been read by that user
	 * consider this as a corpus for user for each document in this corpus, get
	 * the list of words that produce highest TF-IDFs union all of these sets
	 * will give us a list of word with its frequency retain only words iff
	 * frequency greater than a constant the final set will be user profile
	 * Example: userid1 => {soccer, brazil, baseball...}
	 * 
	 * @param userid
	 * @return
	 */
	public static Set<String> computeUserProfile(String userid) {
		List<Document> docs = RecUtil.getReadDocuments(userid);
		if (docs != null && docs.size() > 0) {
			try {
				CorpusUtility util = new CorpusUtility(docs);
				Hashtable<Integer, Integer> sumTFIDF = new Hashtable<Integer, Integer>();
				for (Document doc : docs) {
					Set<Integer> tmpSet = util.getTFIDFSparseVector(doc)
							.getIndexSet();
					for (int wordIndex : tmpSet) {
						if (sumTFIDF.containsKey(wordIndex)) {
							sumTFIDF.put(wordIndex, sumTFIDF.get(wordIndex) + 1);
						} else {
							sumTFIDF.put(wordIndex, 1);
						}
					}
				}

				// remove top words which appear less than cutOff
				Set<String> output = new HashSet<String>();
				Set<Entry<Integer, Integer>> set = sumTFIDF.entrySet();
				for (Entry<Integer, Integer> entry : set) {
					if (entry.getValue() >= RecUtil.CUTOFF_FREQ) {
						output.add(util.getVocab().getWord(entry.getKey()));

					}
				}
				return output;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 
	 * @return
	 */
	private static List<String> genRandomUsers() {

		List<String> userIds = new ArrayList<String>();
		for (int i = 0; i < NUM_USERS; i++) {
			userIds.add("userid" + i);
		}
		return userIds;
	}

	/**
	 * 
	 * @return
	 */
	private static List<Document> genRandomCorpus() {
		String vocabString = "this is a simple test taken from internet each of them may have washington post";
		String[] sampleWords = vocabString.split(" ");
		int len = sampleWords.length;

		List<Document> corpus = new ArrayList<Document>();

		for (int i = 0; i < NUM_DOCS; i++) {

			StringBuffer tmpText = new StringBuffer("document" + i);
			for (int j = 0; j < LEN_DOC; j++) {
				int index = (int) (Math.random() * len);
				tmpText.append(" " + sampleWords[index]);
			}

			Document doc = new Document("docid" + i, "title" + i,
					(int) (Math.random() * NUM_DATE)+"", tmpText.toString());
			corpus.add(doc);
		}
		return corpus;
	}
}
