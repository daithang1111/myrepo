package tn.recommendation;

import java.util.Hashtable;
import java.util.StringTokenizer;

public class Vocabulary {

	private Hashtable<String, Integer> words = null;
	private Hashtable<Integer, String> indexes = null;

	public Vocabulary() {
		words = new Hashtable<String, Integer>();
		indexes = new Hashtable<Integer, String>();
	}

	/**
	 * add a word in vocab if not exists
	 * 
	 * @param word
	 */
	public void addWord(String word) {
		if (Consts.isValid(word)) {
			if (words == null) {
				if (Consts.debug)
					System.out.println("Init vocab");
				words = new Hashtable<String, Integer>();
				indexes = new Hashtable<Integer, String>();
				words.put(word.toLowerCase(), 1);
				//
				indexes.put(1, word.toLowerCase());
			} else {
				if (!words.containsKey(word.toLowerCase())) {
					words.put(word.toLowerCase(), words.size() + 1);
					//
					indexes.put(words.size() + 1, word.toLowerCase());
				}
			}
		}
	}

	/**
	 * get index of word in vocab
	 * 
	 * @param word
	 * @return
	 */
	public int getIndex(String word) {
		if (words == null || !words.containsKey(word.toLowerCase())) {
			if (Consts.debug)
				System.out.println(word + "is not in dictionary");
			return -1;
		} else {
			return words.get(word.toLowerCase());
		}
	}

	/**
	 * get word for an index
	 * 
	 * @param index
	 * @return
	 */
	public String getWord(int index) {
		if (indexes == null || !indexes.containsKey(index)) {
			return "";
		} else {
			return indexes.get(index);
		}

	}

	/**
	 * build vocab from text
	 * 
	 * @param text
	 */
	public void addText(String text) {
		if (Consts.isValid(text)) {
			if (Consts.debug)
				System.out.println("adding " + text);
			StringTokenizer st = new StringTokenizer(text);
			while (st.hasMoreElements()) {
				String word = (String) st.nextElement();
				if (Consts.debug)
					System.out.println("adding " + word);
				addWord(word);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public int getVocabSize() {
		return words.size();
	}

	/**
	 * 
	 * @return
	 */
	public Hashtable<String, Integer> getWords() {
		return words;
	}

	/**
	 * 
	 * @param words
	 */
	public void setWords(Hashtable<String, Integer> words) {
		this.words = words;
	}

	/**
	 * 
	 * @return
	 */
	public Hashtable<Integer, String> getIndexes() {
		return indexes;
	}

	/**
	 * 
	 * @param indexes
	 */
	public void setIndexes(Hashtable<Integer, String> indexes) {
		this.indexes = indexes;
	}

}
