package org.tom.components;

public class Head extends Element {
	public Head() {
		super("head", false);
	}
	public Head(Element element) {
		this();
		addContent(element);
	}
}