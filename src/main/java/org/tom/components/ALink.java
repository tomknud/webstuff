package org.tom.components;

public class ALink extends Element {

	public ALink() {

		super("ALink", false);

	}

	private String text;

	private String link;

	private String onClick;

	private String rel;

	public ALink(String text, String link) {

		this();

		this.text = text;

		this.link = link;

	}

	public void setOnClick(String onClick) {

		this.onClick = onClick;

	}

	public void setRel(String rel) {

		this.rel = rel;

	}

	@Override

	public void print(int depth, StringBuilder sb) {

		indent(depth, sb);

		sb.append("<a href=\"" + link + "\"" + ((onClick != null) ? " onclick=\"" + onClick + "\"" : "")
				+ ((rel != null) ? " rel=\"" + rel + "\"" : "") + ">" + text + "</a>\n");

	}

}