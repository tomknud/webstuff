package org.tom.servlet;

import static org.tom.servlet.Constants.*;

import java.util.*;

public class Context {
	private Map<String,Object> attributes;

	public Context() {
		attributes = new HashMap<>();
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public void setAttribute(String key,Object attribute) {
		attributes.put(key, attribute);
	}

	@Override
	public String toString() {
		return "Path=" + getAttribute(PATH_KEY) + " Session:" + getAttribute(JSESSIONID);
	}
	
}
