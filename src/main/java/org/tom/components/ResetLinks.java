package org.tom.components;

import static org.tom.servlet.Constants.*;

import org.tom.servlet.*;
import static org.tom.components.KSFCore.*;

public class ResetLinks extends View {

	public ResetLinks(Context session) {
		addContent(new Head(new JSFunctions()));
		Body body = new Body();
		addContent(body);
		body.addContent(addreadLinksForm((String) session.getAttribute(JSESSIONID)));
		assignUniqueIds();
	}

	private Form addreadLinksForm(String sessionKey) {
		Form form = new Form();
		form.putMethod(GET);
		form.putAction(READ_PAGE);
		form.addContent(new Header("Pick a Page"));
		form.addContent(addHiddenInput(SESSION_KEY, sessionKey));
		Table table = new Table();
		form.addContent(table);
		table.addContent(addInputRow("Page Address       ", PAGE_ADDRESS));
		table.addContent(addSubmitRow("Submit"));
		table.addContent(addResetRow ("Reset"));
		table.addContent(addButtonRow("Button"));
		return form;
	}

}
