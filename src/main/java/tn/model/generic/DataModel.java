package tn.model.generic;

public class DataModel {

	Actor actor;
	Document doc;
	DataGroup dataGroup;

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

	public DataGroup getDataGroup() {
		return dataGroup;
	}

	public void setDataGroup(DataGroup dataGroup) {
		this.dataGroup = dataGroup;
	}

	@Override
	public String toString() {

		return dataGroup.toString() + "\t" + actor.toString() + "\t"
				+ doc.toString();

	}
}
