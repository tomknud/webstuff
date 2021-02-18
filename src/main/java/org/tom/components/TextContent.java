package org.tom.components;

public class TextContent extends Element {
	public TextContent(String text) {
		super(text, true);
	}
	public void print(int depth, StringBuilder sb) {
		indent(depth, sb);
		sb.append(getOpen() + "\n");
	}
}