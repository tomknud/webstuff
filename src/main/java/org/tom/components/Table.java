package org.tom.components;

public class Table extends Element {

	public Table() {

		super("table", false);

	}

	public Table(String text) {

		this();

		addContent(new TextContent(text));

	}

	public static Table add(Table asdf, Object fdsa) {

		return asdf;

	}

}