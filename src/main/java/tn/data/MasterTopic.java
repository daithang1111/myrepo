package tn.data;

import java.util.ArrayList;
import java.util.List;

public class MasterTopic {
	private String name;
	private int code;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	private List<Topic> topics;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Topic> getTopics() {
		if (topics == null) {
			topics = new ArrayList<Topic>();
		}
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	@Override
	public String toString() {
		return "MasterTopic [name=" + name + ", code=" + code + ", topics="
				+ topics + "]";
	}

}
