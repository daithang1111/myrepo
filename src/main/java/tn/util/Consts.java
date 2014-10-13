package tn.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.util.Version;

import tn.model.generic.Actor;
import tn.model.generic.DataModel;
import tn.model.generic.Document;
import tn.model.recommendation.Vocabulary;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Consts {

	public static final Date getTIMESTAMP_CONST() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		return cal.getTime();
	}

	public static final long getTIME_CONST() {
		return 0;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> readFileAsList(String file) {
		List<String> terms = new ArrayList<String>();
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			fstream = new FileInputStream(file);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String strLine = "";
			while ((strLine = br.readLine()) != null) {
				if (strLine.trim().length() > 0) {
					terms.add(strLine);
				}
			}
			br.close();
			in.close();
		} catch (IOException e) {

		} finally {
			closeQuietly(br);
			closeQuietly(in);
			closeQuietly(fstream);
		}
		return terms;
	}

	public static String readFile(String file) {
		StringBuffer buf = new StringBuffer();
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			fstream = new FileInputStream(file);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String strLine = "";
			while ((strLine = br.readLine()) != null) {
				buf.append(strLine + "\n");
			}
			br.close();
			in.close();
		} catch (IOException e) {

		} finally {
			closeQuietly(br);
			closeQuietly(in);
			closeQuietly(fstream);
		}
		return buf.toString();
	}

	/**
	 * 
	 * @param closeable
	 */
	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException ex) {
				// ignore
			}
		}
	}

	// /**
	// *
	// * @param str
	// * @param fileName
	// * @param append
	// */
	// @Deprecated
	// public static void fileWriter(String str, String fileName, boolean
	// append) {
	// try {
	// BufferedWriter output = new BufferedWriter(new FileWriter(fileName,
	// append));
	// output.write(str);
	// output.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public static void fileWriter(String str, String fileName, boolean append) {
		try {
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(fileName), append), "UTF-8"));

			output.write(str);
			output.flush();
			output.close();

		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * convert tab delim text to Document
	 * 
	 * @param input
	 * @return
	 */
	public static Document toDocument(String input) {
		if (input == null)
			return null;
		String[] values = input.split("\\t");
		if (values.length != 4) {
			return null;
		}

		String docId = values[0];
		String title = values[1];
		// parse date
		long dateLong = Consts.getTIME_CONST();
		SimpleDateFormat parserSDF = new SimpleDateFormat(
				"EEE MMM d HH:mm:ss zzz yyyy");
		try {
			Date date = parserSDF.parse(values[2]);
			dateLong = date.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//

		String text = values[3];

		return new Document(dateLong, docId, title, text);
	}

	public static DataModel toDataModel(String input) {
		if (input == null)
			return null;
		String values[] = input.split("\\t");
		if (values.length != 7)
			return null;

		DataModel dm = new DataModel();
		// parse date
		Date timestamp = Consts.getTIMESTAMP_CONST();
		SimpleDateFormat parserSDF = new SimpleDateFormat(
				"EEE MMM d HH:mm:ss zzz yyyy");
		try {
			timestamp = parserSDF.parse(values[0]);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dm.setTimestamp(timestamp);

		// actor
		Actor actor = new Actor();
		actor.setActorId(values[1]);
		actor.setActorName(values[2]);
		dm.setActor(actor);
		// document
		String docId = values[3];
		String title = values[4];
		long dateLong = Consts.getTIME_CONST();
		try {
			Date date = parserSDF.parse(values[5]);
			dateLong = date.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String text = values[6];

		Document doc = new Document(dateLong, docId, title, text);
		dm.setDoc(doc);

		return dm;

	}

	/**
	 * 
	 * @param text
	 * @param stop_words
	 * @param neFinder
	 * @return
	 */
	public static List<String> cleanText(String text, List<String> stop_words,
			NEFinder neFinder) {
		String cleaned = text.toLowerCase().replaceAll("[\\r\\n]+", " ");
		StringTokenizer st = new StringTokenizer(cleaned);
		List<String> words = Lists.newArrayList();
		while (st.hasMoreElements()) {
			words.add((String) st.nextElement());
		}
		words.removeAll(stop_words);
		cleaned = Joiner.on(" ").join(words);

		if (neFinder != null) {
			cleaned = neFinder.replaceNER(cleaned);
		}
		try {
			words = AnalyzerUtils.parse(new SimpleAnalyzer(Version.LUCENE_45),
					cleaned);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		words.removeAll(stop_words);

		return words;
	}

	/**
	 * 
	 * @param vocab
	 * @param outputDir
	 */
	public static void printVocab(Vocabulary vocab, String outputDir) {
		String dicFile = outputDir + "/vocab.txt";
		Set<Entry<String, Integer>> set = vocab.getWords().entrySet();
		List<String> list = new ArrayList<String>();
		for (Entry<String, Integer> entry : set) {
			list.add(entry.getKey() + "\t" + entry.getValue());
		}
		Consts.fileWriter(Joiner.on("\n").join(list), dicFile, false);
	}
}
