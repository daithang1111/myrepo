package tn.model.codebook;

import java.util.ArrayList;
import java.util.List;

public class MasterTopicCode {
	private String name;
	private int code;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	private List<TopicCode> topics;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TopicCode> getTopics() {
		if (topics == null) {
			topics = new ArrayList<TopicCode>();
		}
		return topics;
	}

	public void setTopics(List<TopicCode> topics) {
		this.topics = topics;
	}

	@Override
	public String toString() {
		return "MasterTopic [name=" + name + ", code=" + code + ", topics="
				+ topics + "]";
	}

}
