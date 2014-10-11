package tn.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Consts {

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

	/**
	 * 
	 * @param str
	 * @param fileName
	 * @param append
	 */
	public static void fileWriter(String str, String fileName, boolean append) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(fileName,
					append));
			output.write(str);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
