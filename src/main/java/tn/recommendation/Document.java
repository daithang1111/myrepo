package tn.recommendation;

public class Document {

	private String docid;
	private String title;
	private String text;
	private long date;

	public Document(long date, String docid, String title, String text) {
		this.setDocid(docid);
		this.setTitle(title);
		this.setDate(date);
		this.setText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public String getDocid() {
		return docid;
	}

	public void setDocid(String docid) {
		this.docid = docid;
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

	@Override
	public String toString() {
		return docid + "\t" + date + "\t" + title + "\t" + text;
	}

}
