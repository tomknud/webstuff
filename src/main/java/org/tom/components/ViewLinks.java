package org.tom.components;

import static org.tom.components.KSFCore.*;
import static org.tom.servlet.Constants.*;
import static org.tom.session.Manager.*;

import java.nio.file.*;
import java.util.*;

import org.tom.servlet.*;

public class ViewLinks extends View {

	private Body body;
	public ViewLinks(Context context) {
		addContent(new Head(new JSFunctions()));
		body = new Body();
		addContent(body);
		body.addContent(addreadLinksForm(context));
		assignUniqueIds();
	}
	private Table table;
	private Form addreadLinksForm(Context context) {
		String sessionKey = (String) context.getAttribute(JSESSIONID);
		Form form = new Form();
		form.setId(READ_LINKS);
		form.putMethod(POST);
		form.putAction(SAVE_LINKS);
		form.addContent(new Header("Links from page"));
		form.addContent(addHiddenInput(SESSION_KEY, sessionKey));
		ALink linkO = new ALink("New Page", "startLinks/?" + SESSION_KEY + "=" + sessionKey);
		form.addContent(linkO);
		table = new Table();
		table.setId(INPUTS);
		form.addContent(table);
		getLinksFromSession(context).stream().filter(record -> !getFilteredFromSession(context, record))
		.map(link -> recordToRow(link)).reduce(table, (tableO, fdsa) -> (Table) tableO.addContent(fdsa), Table::add);
		table.addContent(addSubmitRow(SUBMIT));
		table.addContent(addResetRow(RESET));
		table.addContent(addButtonRow(BUTTON));
		table.setConsumer(this::consumer);
		return form;
	}

	public Row recordToRow(Record record) {
		Row row = new Row();
		Data d = new Data();
		Input input = new Input();
		input.setType(CHECKBOX);
		input.setName(NULL_STRING + record.getLink().hashCode());
		ALink linkO = new ALink(record.getText(), record.getLink());
		input.addContent(linkO);
		d.addContent(input);
		row.addContent(d);
		return row;
	}

	@SuppressWarnings("unchecked")
	private void consumer(Context context,Element element) {
		if(((Path)context.getAttribute(PATH_KEY)).endsWith("resetLinks")) {
			context.setAttribute(PATH_KEY,null);
			context.setAttribute(NEW_PATH_KEY,Paths.get("faces\\links\\startLinks"));
			invalidateSession(context);
		} else if(((Path)context.getAttribute(PATH_KEY)).endsWith("startLinks")) {
			context.setAttribute(PATH_KEY,null);
			context.setAttribute(NEW_PATH_KEY,Paths.get("faces\\links\\startLinks"));
		}	else {
			ensureSession(context);// Same code, new context
			Map<String, String> filtered = (Map<String, String>) context.getAttribute(DATA);
			Set<Integer> filters = (Set<Integer>) context.getAttribute(FILTER);
			filtered.keySet().stream().filter(name -> nonLinkFilterable(name)).forEach(key -> filters.add(Integer.parseInt(key)));
			context.getAttribute(NEW_PATH_KEY).equals(context.getAttribute(PATH_KEY));
			// TODO : So as to reload this exe, nuke prior path. In future send change set via ajax
			context.setAttribute(PATH_KEY,null);
			context.setAttribute(NEW_PATH_KEY,Paths.get("faces\\links\\readLinks"));
		}
	}

	@SuppressWarnings("unchecked")
	public void ensureSession(Context session) {
		if(session.getAttribute(LINKS) == null || session.getAttribute(FILTER) == null) {
			Context priorSession = (Context) session.getAttribute(HISTORY);
			if(priorSession != null) {
				List<Record> links = (List<Record>) priorSession.getAttribute(LINKS);
				if (links != null) {
					session.setAttribute(LINKS, links);
				}
				Set<Integer> filters = ((Set<Integer>) priorSession.getAttribute(FILTER));
				if (filters != null) {
					session.setAttribute(FILTER, filters);
				}
			} else {
				// BONK
				System.out.println("Throw a chair!");
			}
		}
	}

	public boolean nonLinkFilterable(String name) {
		boolean result = true;
		result = !SESSION_KEY.equals(name);
		result &= !PAGE_ADDRESS.equals(name);
		return result;
	}

}
