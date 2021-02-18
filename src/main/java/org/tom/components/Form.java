package org.tom.components;

public class Form extends Element {

	public Form() {

		super("form", false);

	}

	public void putAction(String action) {

		setParameter("action", action);

	}

	public void putMethod(String method) {

		setParameter("method", method);

	}

}