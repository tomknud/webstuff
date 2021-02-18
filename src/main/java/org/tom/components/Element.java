package org.tom.components;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.tom.header.*;
import org.tom.header.Header;
import org.tom.servlet.*;

import static org.tom.servlet.Constants.*;

abstract public class Element implements Page {
	private Element() {
//		setConsumer((context,element)->System.out.println("execute:"+getClass().getCanonicalName()) );
		setConsumer((context,element)->System.out.print("") );
	}
	public Element(String open, boolean single) {
		this();
		this.open = open;
		this.single = single;
		if(needId) {
			setId("NoId");
		}
	}
	private static String prefix[] = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	private Element parent;
	protected List<Element> content = new ArrayList<Element>();
	private boolean single = false;
	private String open;
	private Map<String, String> parameters = new HashMap<>();
	protected boolean needId = false;
	public void setId(String id) {
		setParameter("id", id);
	}
	public String getId() {
		return parameters.get("id" + "=");
	}
	public void setClass(String clacc) {
		setParameter("id", clacc);
	}
	public void setParameter(String name, String value) {
		parameters.put(name + "=", "\"" + value + "\"");
	}
	public String getParameter(String name) {
		if(parameters.get(NAME + "=") != null) {
			return parameters.get(NAME + "=").replace("\"", "");
		}
		return null;
	}
	public boolean isName(String name) {
		if(parameters.get(NAME + "=") != null) {
			return parameters.get(NAME + "=").replace("\"", "").equals(name);
		}
		return false;
	}
	public Element addContent(Element element) {
		content.add(element);
		element.setParent(this);
		return this;
	}
	public void assignUniqueIds() {
		if(parent == null) {
			getNewIdLocals(content,0);
		} else {
			parent.assignUniqueIds();
		}
	}
	private Map<String,Integer> levelCounts;
	public Map<String,Element> elements;
	private void getNewIdLocals(List<Element> content, int depth) {
		content.forEach(element->{element.setId(getLevelId(depth));elements.put(element.getId(), element);getNewIdLocals(element.content,depth+1);});
	}
	private String getLevelId(int depth) {
		if(levelCounts == null) {
			levelCounts = new HashMap<>();
			elements = new HashMap<>();
		}
		Integer count = levelCounts.get(prefix[depth]);
		if(count == null) {
			count = 1;
		} else {
			count += 1;
		}
		levelCounts.put(prefix[depth], count);
		return prefix[depth]+count;
	}
	public Element getParent() {
		return parent;
	}
	public void setParent(Element parent) {
		this.parent = parent;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void apply(Context context) {
//		System.out.println("apply:"+this.getClass().getCanonicalName());
		if(this instanceof Input) {
			((Input)this).setValue(((Map<String,String>)context.getAttribute(DATA)).get(getParameter(NAME)));
		} else {
			content.forEach(element->element.apply(context));
		}
	}
	public Object findInputValue(String name) {
		Optional<Element> any = null;
		if(parent == null) {
			any = elements.values().stream().filter(element->element.isName(name)).findAny();
		} else {
			return parent.findInputValue(name);
		}
		return ((Input)any.orElse(this)).getValue();
	}
	public Object findInputValueBad(String name) {
		Object value = null;
		if(this instanceof Input) {
			if(name.equals(getParameter(NAME))) {
				value =  ((Input)this).getValue();
			}
		} else {
			List<Element> op = content.stream().flatMap(element->element.content.stream()).collect(Collectors.toList());
			Optional<Element> opp = op.stream().filter(element->element.isName(name)).findFirst();
			value = (String) ((Input)opp.get()).getValue();
		}
		return value;
	}
	private BiConsumer<Context,Element> consumer;
	public void setConsumer(BiConsumer<Context, Element> consumer) {
		this.consumer = consumer;
	}
	@Override
	public void execute(Context context) {
		consumer.accept(context,this);
		content.forEach(element->element.execute(context));
	}
	@Override
	public void encode(Context context) throws IOException {
		StringBuilder sb = new StringBuilder();
		print(0, sb);
		OutputStream rawOut = (OutputStream) context.getAttribute(WRITER_KEY);
		int length = sb.length();
		writeTextHeader(context, length);
		rawOut.write(sb.toString().getBytes());
		rawOut.flush();
		System.out.println("Sent:"+getClass().getCanonicalName());		
	}

	public void print(int depth, StringBuilder sb) {
		appendOpen(depth, sb);
		int newDepth = depth + 1;
		content.forEach(element -> element.print(newDepth, sb));
		appendClose(depth, sb);
	}

	private void appendClose(int depth, StringBuilder sb) {
		if (!single) {
			indent(depth, sb);
			sb.append(CLOSED);
			sb.append(open);
			sb.append(CLOSE + "\n");
		}
	}

	private void appendOpen(int depth, StringBuilder sb) {
		indent(depth, sb);
		sb.append(OPEN);
		sb.append(open);
		sb.append(" ");
		parameters.forEach((k, v) -> {
			sb.append(k + v);
			sb.append(" ");
		});
		sb.append(single ? CLOSES : CLOSE);
		sb.append("\n");
	}

	@SuppressWarnings("unchecked")
	public void writeTextHeader(Context context, int length) {
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter((OutputStream) context.getAttribute(WRITER_KEY))));
		Map<HeaderKey,Header> output_headers = (Map<HeaderKey, Header>) context.getAttribute(OUTPUT_HEADERS);
		out.println("HTTP/1.0 200 OK");
		if(!output_headers.isEmpty()) {
			for(Header header: output_headers.values()) {
				out.println(header.getKey() + ": " + header.getContent());
			}
		}
		out.println("Content-Length: " + length);
		out.println("Content-Type: text/html");
		out.println("");
		out.flush();
	}

	protected void indent(int depth, StringBuilder sb) {
		for (int i = 0; i < depth; i++) {
			sb.append("\t");
		}
	}

	protected String getOpen() {
		return open;
	}

}