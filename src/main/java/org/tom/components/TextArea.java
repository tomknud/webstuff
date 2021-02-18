package org.tom.components;

import static org.tom.servlet.Constants.*;

public class TextArea extends Element {
	public TextArea() {
		super("textarea", false);
	}
	public void setType(String type) {
		setParameter("type", type);
	}
	public void setName(String name) {
		setParameter(NAME, name);
	}
}