package org.tom.components;

public class Header extends Element {

	public Header(String level) {
		super(level, false);
	}

	public Header(int level, String text) {
		this(level == 3 ? "H3" : "H1");
		addContent(new TextContent(text));
	}

}