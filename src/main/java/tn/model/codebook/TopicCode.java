package tn.model.codebook;

import java.util.List;

public class TopicCode {
	private String name;
	private int code;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	private String type;
	private List<String> keywords;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Topic [name=" + name + ", code=" + code + ", type=" + type
				+ ", keywords=" + keywords + "]";
	}

}
