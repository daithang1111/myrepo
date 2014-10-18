package tn.model.generic;


public class Document {

	private String docId;
	private String docTitle;
	private String docTime;
	private String docContent;

	public Document(String docid, String title, String date, String text) {

		this.docId = docid;
		this.docTitle = title;
		this.docTime = date;
		this.docContent = text;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getDocTitle() {
		return docTitle;
	}

	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}

	public String getDocContent() {
		return docContent;
	}

	public void setDocContent(String docContent) {
		this.docContent = docContent;
	}

	public String getDocTime() {
		return docTime;
	}

	public void setDocTime(String docTime) {
		this.docTime = docTime;
	}

	@Override
	public String toString() {
		return docId + "\t" + docTitle + "\t" + docTime + "\t" + docContent;
	}
}
