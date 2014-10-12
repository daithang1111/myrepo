package tn.model.generic;

import java.util.Calendar;

public class Document {

	private String docId;
	private String title;
	private String text;
	private long date;

	public Document(long date, String docid, String title, String text) {
		this.setDate(date);
		this.setDocId(docid);
		this.setTitle(title);
		this.setText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date2) {
		this.date = date2;
	}

	public String getDateString() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		return cal.getTime().toString();
	}

	@Override
	public String toString() {
		return docId + "\t" + title + "\t" + getDateString() + "\t" + text;
	}

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		Document doc = new Document(cal.getTimeInMillis(), "title1", "doc1",
				"this is a test");

		System.out.println(doc.getDateString());
	}
}
