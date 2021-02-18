package org.tom.servlet;

import java.io.*;

public interface Page {

	public void apply(Context context);

	public void execute(Context context);

	public void encode(Context context) throws IOException;

}
