package tn.recommendation;

/**
 * @author nguyentd4
 * 
 * NOTES: 
 * 1. this simple implementation assumes that we don't have too many documents, or we just care about the recent 1 week of news articles. 
 * If so many documents ~1M++, we may first need to call LSH algorithm to filter out pairs that are not so similar first.
 * 
 * 2. the class CorpusUtility contains convenient methods to work with text, including compute TF-IDF and jaccard/cosine scores for documents.
 * This class, if having huge number of documents, stored in HDFS, we can use several mapreduce programs to do the same things. Especially, the
 * precomputation of inverted-index. We also want to fix the vocabulary size if possible, for a new document, we donot' need to recompute IDF, but 
 * only TF which is simple.
 * 
 * 3. an article (document) contains 4 fields: docid, title, date in long (more like exact time in milliseconds since 1970s), and text. we also assume that document
 * length is reasonable (short ones get filtered before going to this step, and lowercased).
 * 
 * 4. the MatchedDocument is kind of lengthy, it contains score and document which implements comparator for easy sorting (descending, first by 
 * date and then by score), of course we can sort by score and actual date
 * 
 * ALGORITHMS
 * 
 * PART1: Recommend articles that being currently read by users who read content similar to
 me. Assume database of userid-documentid mapping exists

 SOLUTION: getDocumentsBasedOnUserSimilarity

 For this part, the key is to get all users who read content similar to an userid:
 1) Build a profile for an user by considering all articles that she/he has read recently (how far we should go?)
 1.1) assume that we do not have labeled data, but only texts. First consider these articles as a corpus
 1.2) for each article in this corpus, compute TF-IDF for each word (term) in this corpus, filtered-out by a threshold
 Ex: input: article1 = "this is a nice algorithm"
 output: {nice, algorithm}
 1.3) merge these lists together for all articles, add total frequency for each term
 1.4) retain only terms that have total frequency >THRESHOLD (we can do fixed length also)
 Ex: input {nice:2, algorithm:3, hash:4, item:1}, THRESHOLD =3 ----->
 output {algorithm, hash}
 2) Build profile for all users who are reading articles
 3) Compare two sets of profiles using Jaccard score, retain users with high scores
 4) Get reading articles for those users
 --------NOTE: - we should compute user profiles by a batch job on all userids and update these one in a while since the profile may not
 change so rapidly. - easier method is to use just documentid sets as profile for userid, but this create a very sparse matrix which may/not
 produce good results 

 PART2:  Articles related to the current article I am reading - in the order of
 most recent first. Relevancy based off of NLP processing of the current article.

 SOLUTION: getDocumentsBasedOnDocumentSimilarity

 For this part, we need to compare the current article with certain number of recent articles (last week for example)
 The key is to get important key words from current article then compare with key words from others
 1) Similarly to the above, we use TF-IDF to select some important words for current article (and all others too-batch job)
 2) Calculate similarity based on Jaccard/or cosine
 3) Return top articles based on time (data) and then score




 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class Main {

	public static void main(String[] args) {
		Main main = new Main();

	}

	/**
	 * can use LSH to get similar candidates pairs before computing the jaccard
	 * score
	 * 
	 * @param docid
	 * @return
	 */
	public MatchedDocument[] getDocumentsBasedOnDocumentSimilarity(String docid) {

		if (Consts.isValid(docid)) {
			if (Consts.debug)
				System.out.println("Getting similar documents for docid = "
						+ docid);
			try {
				Document doc = Consts.getDocument(docid);
				List<Document> comparedDocs = Consts.getRecentDocuments();
				if (comparedDocs != null && comparedDocs.size() > 0) {
					List<MatchedDocument> matchedDocuments = new ArrayList<MatchedDocument>();
					double score = 0;

					// build corpus based on all recent documents
					CorpusUtility util = new CorpusUtility(comparedDocs);

					for (Document cDoc : comparedDocs) {
						score = util.computeJaccardScore(doc, cDoc);
						if (score >= Consts.DOC_SIMILARITY_THRESHOLD) {

							matchedDocuments.add(new MatchedDocument(cDoc,
									score));
						}
					}
					MatchedDocument[] output = matchedDocuments
							.toArray(new MatchedDocument[matchedDocuments
									.size()]);

					Arrays.sort(output);

					return output;
				} else {
					System.out.println("No documents in corpus to compare");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Invalid docid");
		}
		return null;
	}

	/**
	 * 
	 * @param userid
	 * @param scoreThreshold
	 * @return
	 */
	public MatchedDocument[] getDocumentsBasedOnUserSimilarity(String userid) {

		if (Consts.isValid(userid)) {
			if (Consts.debug)
				System.out.println("Getting documents for userid = " + userid);
			try {
				Hashtable<String, Double> similarUsers = getSimilarUsers(userid);

				Set<Entry<String, Double>> set = similarUsers.entrySet();
				double score;

				List<MatchedDocument> matchedDocuments = new ArrayList<MatchedDocument>();
				for (Entry<String, Double> entry : set) {
					score = entry.getValue();
					if (score >= Consts.USER_SIMILARITY_THRESHOLD) {

						Document doc = Consts
								.getReadingDocument(entry.getKey());
						if (doc != null)
							matchedDocuments
									.add(new MatchedDocument(doc, score));
					}
				}

				MatchedDocument[] output = matchedDocuments
						.toArray(new MatchedDocument[matchedDocuments.size()]);

				Arrays.sort(output);

				return output;
			} catch (Exception e) {

			}
		} else {
			System.out.println("Please give correct userid");

		}
		return null;
	}

	/**
	 * get list of similar users to this user, together with similarity scores
	 * TODO:
	 * 
	 * @param userid
	 * @return
	 */
	private Hashtable<String, Double> getSimilarUsers(String userid) {
		try {
			Set<String> userProfile = Consts.computeUserProfile(userid);
			if (Consts.debug) {
				System.out.println("Profile for userid = " + userid + ":");
				for (String w : userProfile) {
					System.out.println("word: " + w);
				}
			}
			if (userProfile != null && userProfile.size() > 0) {
				List<String> readingUsers = Consts.getReadingUsers();
				if (readingUsers != null && readingUsers.size() > 0) {

					Hashtable<String, Double> output = new Hashtable<String, Double>();

					for (String readingUser : readingUsers) {
						if (Consts.debug) {
							System.out.println("userid:" + readingUser);

						}
						Set<String> readingUserProfile = Consts
								.computeUserProfile(readingUser);
						double score = Consts.getJaccardScoreForWordSets(
								userProfile, readingUserProfile);
						output.put(readingUser, score);
					}

					return output;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
