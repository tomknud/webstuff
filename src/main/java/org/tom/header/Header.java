package org.tom.header;

import static org.tom.session.Manager.*;

public class Header {
	private HeaderKey key;
	private String content;

	public Header(String line) {
		super();
		this.key = getHeaderKey(line);
		this.content = key.getValue();
	}

	public Header(HeaderKey key, String content) {
		super();
		this.key = key;
		this.content = content;
	}

	public HeaderKey getKey() {
		return key;
	}

	public void setKey(HeaderKey key) {
		this.key = key;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Header [key=" + key + ", content=" + content + "]";
	}

}