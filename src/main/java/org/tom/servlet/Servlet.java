package org.tom.servlet;

public interface Servlet {
	public void service(Context context);
	public void setNext(Servlet servlet);
	public Servlet getNext();
}
