package org.tom.components;

import static org.tom.servlet.Constants.*;

public class Input extends Element {
	private Object value;
	private Class<?> type;
	public Input() {
		super("input", true);
	}

	public void setType(String value) {
		setParameter("type", value);
	}

	public void setName(String value) {
		setParameter(NAME, value);
	}

	public Object getName() {
		return getParameter(NAME);
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

}