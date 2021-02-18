package org.tom.components;

public class Button extends Element {
	public Button() {
		super("button", false);
	}
	public Button(String string) {
		this();
		addContent(new TextContent(string));
	}
	public void setType(String value) {
		setParameter("type", value);
	}
}