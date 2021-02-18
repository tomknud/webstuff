package org.tom.components;

import static org.tom.servlet.Constants.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import org.tom.application.*;
import org.tom.servlet.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import static org.tom.components.KSFCore.*;

public class ReadLinks extends View {
	private XmlMapper mapper = new XmlMapper();

	public ReadLinks(Context context) {
		addContent(new Head(new JSFunctions()));
		Body body = new Body();
		addContent(body);
		body.addContent(addreadLinksForm((String) context.getAttribute(JSESSIONID)));
		assignUniqueIds();
	}

	private Form addreadLinksForm(String sessionKey) {
		Form form = new Form();
		form.putMethod(POST);
		form.putAction(READ_PAGE);
		form.addContent(new Header("Pick a Page"));
		form.addContent(addHiddenInput(SESSION_KEY, sessionKey));
		Table table = new Table();
		form.addContent(table);
		table.addContent(addInputRow("Page Address       ", PAGE_ADDRESS));
		table.addContent(addSubmitRow("Submit"));
		table.addContent(addResetRow ("Reset"));
		table.addContent(addButtonRow("Button"));
		table.setConsumer(this::consumer);
		return form;
	}
	private void consumer(Context context,Element element) {
		ensureSession(context);
		String result = null;
		String url = (String) findInputValue(PAGE_ADDRESS);
		if(url == null) {
			// for now, this is chicken egg, first access won't have url and doesn't need execution
			return;
		}
		try {
			result = SSLSocketClientWithClientAuth.getFromURLTLS(url);
		} catch (Exception e) {
			System.err.println("e:" + e.getMessage());
			return;
		}

		Pattern pattern = Pattern.compile("<a ");
		Pattern patternB = Pattern.compile("</a>");
		Matcher matcher = pattern.matcher(result);

		while (matcher.find()) {
			String start = result.substring(matcher.start());
			Matcher matcherB = patternB.matcher(start);
			if (matcherB.find()) {
				String link = start.substring(0, matcherB.start() + 4);
				Record data = processLink(link);
				if (((data != null) && !getFilteredFromSession(context, data)) && !getLinksFromSession(context).contains(data)) {
					getLinksFromSession(context).add(data);
				}
			}
		}
		if(context.getAttribute(NEW_PATH_KEY).equals(context.getAttribute(PATH_KEY))) {
			context.setAttribute(PATH_KEY,null);// new path got us here, but this is NOT the viewer so bump it
		}
		context.setAttribute(NEW_PATH_KEY,Paths.get("faces\\links\\readLinks"));
	}
	
	public Record processLink(String input) {
		Record data = null;
		String link = null;
		String text = null;
		try {
			JsonNode thing = mapper.readTree(input);
			if (thing.get(LINK_KEY) != null) {
				link = thing.get(LINK_KEY).toString();
			}

			if (thing.get(NULL_KEY) == null) {
				JsonNode thingB = thing.get(SPAN_KEY);
				if (thingB != null) {
					JsonNode textB = thingB.get(NULL_KEY);
					if (textB != null) {
						text = textB.toString();
					}
				} else {
					JsonNode thingC = thing.get(IMAGE_KEY);
					if (thingC != null) {
						text = IMAGE_KEY;
					}
				}
			} else {
				text = thing.get(NULL_KEY).toString();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		if (text != null && link != null) {
			text = text.replace(QUOTE, NULL_STRING);
			text = text.replace(SPACE, UNDERSCORE);
			link = link.replace(QUOTE, NULL_STRING);
			data = new Record(text, link);
			System.out.println(data.toString());
		}
		return data;
	}

	public void ensureSession(Context session) {
		List<Record> links = (List<Record>) session.getAttribute(LINKS);
		if (links == null) {
			session.setAttribute(LINKS, new ArrayList<Record>());
		}
		Set<Integer> filters = ((Set<Integer>) session.getAttribute(FILTER));
		if (filters == null) {
			session.setAttribute(FILTER, new HashSet<Integer>());
		}
	}

}
