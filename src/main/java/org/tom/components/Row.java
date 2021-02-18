package org.tom.components;

public class Row extends Element {

	public Row() {

		super("tr", false);

	}

	public Row(String text) {

		this();

		addContent(new TextContent(text));

	}

}