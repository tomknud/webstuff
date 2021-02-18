package org.tom.components;

public class Data extends Element {

	public Data() {

		super("td", false);

	}

	public Data(String text) {

		this();

		addContent(new TextContent(text));

	}

}