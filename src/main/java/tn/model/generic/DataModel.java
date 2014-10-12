package tn.model.generic;

import java.util.Date;

public class DataModel {

	Actor actor;
	Document doc;
	Date timestamp;

	public Actor getActor() {
		return actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {

		return timestamp.toString() + "\t" + actor.toString() + "\t"
				+ doc.toString();

	}
}
