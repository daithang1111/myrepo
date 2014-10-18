package tn.model.recommendation;

import java.util.Comparator;

import tn.model.generic.Document;

public class MatchedDocument implements Comparator<MatchedDocument>,
		Comparable<MatchedDocument> {
	private Document doc;
	private double score;

	public MatchedDocument(Document doc, double score) {
		this.setDoc(doc);
		this.setScore(score);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatchedDocument other = (MatchedDocument) obj;
		if (doc == null) {
			if (other.doc != null)
				return false;
		} else if (!doc.equals(other.doc))
			return false;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		return true;
	}

	public static final Comparator<MatchedDocument> scoreComparator = new Comparator<MatchedDocument>() {

		public int compare(MatchedDocument doc1, MatchedDocument doc2) {

			double score1 = doc1.getScore();
			double score2 = doc2.getScore();
			// descending order
			if (score1 > score2) {
				return -1;
			} else if (score1 == score2) {
				return 0;
			} else {
				return 1;
			}
		}

	};

	public static final Comparator<MatchedDocument> docComparator = new Comparator<MatchedDocument>() {

		public int compare(MatchedDocument doc1, MatchedDocument doc2) {

			String title1 = doc1.getDoc().getDocTitle().toLowerCase();
			String title2 = doc2.getDoc().getDocTitle().toLowerCase();

			return title1.compareTo(title2);

		}

	};

	public int compareTo(MatchedDocument comparedDoc) {

		String currentDate = doc.getDocTime();
		String comparedDate = comparedDoc.doc.getDocTime();

		if (currentDate.compareTo(comparedDate) > 0) {
			return -1;
		} else if (currentDate == comparedDate) {
			if (score > comparedDoc.getScore()) {
				return -1;
			} else if (score == comparedDoc.getScore()) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	@Override
	public int compare(MatchedDocument doc1, MatchedDocument doc2) {
		double score1 = doc1.getScore();
		double score2 = doc2.getScore();
		String date1 = doc1.getDoc().getDocTime();
		String date2 = doc2.getDoc().getDocTime();

		if (date1.compareTo(date2) > 0) {
			return -1;
		} else if (date1 == date2) {
			if (score1 > score2) {
				return -1;
			} else if (score1 == score2) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}
}
