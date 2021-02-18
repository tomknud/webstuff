package org.tom.servlet;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

import org.tom.components.*;

import static org.tom.servlet.Constants.*;

public class FacesServlet extends ExecutableServlet {
	private Map<Path,Class<? extends Page>> installedPages = new HashMap<>();
	{
		installedPages.put(Paths.get("faces\\links\\startLinks"),	ReadLinks.class);
		installedPages.put(Paths.get("faces\\links\\resetLinks"),	ResetLinks.class);
		installedPages.put(Paths.get("faces\\links\\readLinks"),	ViewLinks.class);
		installedPages.put(Paths.get("faces\\links\\saveLinks"),	ViewLinks.class);
	}

	public FacesServlet(String docroot, Path path) {
		super(docroot, path);
	}

	@Override
	public void process(Context context) {
		try {
			Phase phase = Phase.IDENTIFICATION;
			Page page = null;
			while(!phase.equals(Phase.COMPLETE)) {
				switch(phase) {
					case IDENTIFICATION : 
						page = ensurePage(context);
						phase = Phase.APPLY;
					case APPLY : 
						page.apply(context);
						phase = Phase.EXECUTION;
					case EXECUTION      : 
						page.execute(context);// Do the business and derive new site
						phase = context.getAttribute(NEW_PATH_KEY).equals(context.getAttribute(PATH_KEY)) ? Phase.ENCODING : Phase.BRANCH;
						break;
					case BRANCH			: 
						page = findPage(context);
						phase = Phase.ENCODING;
					case ENCODING       : 
						page.encode(context);
						phase = Phase.FINALIZING;
					case FINALIZING     : 
						// add jscripts or whatever
						phase = Phase.COMPLETE;
					case COMPLETE       : 
						System.out.println("Sent file contents for " + printThread(context.getAttribute(PATH_KEY).toString()));
						break;
				}
			}
		} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ioe) {
			System.out.println("Failed Path Load " + printThread(context.getAttribute(PATH_KEY).toString()));
		}
	}

	public Page ensurePage(Context context) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {
		Page page;
		Context priorAccess = (Context) context.getAttribute(HISTORY);
		if(priorAccess != null) {
			page = (Page) priorAccess.getAttribute(EXECUTABLE);
			if(page == null) {// ?
				page = findPage(context);
			} else {
				context.setAttribute(EXECUTABLE, page);
			}
		} else {
			page = findPage(context);
		}
		return page;
	}

	private Page findPage(Context context) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		Class<? extends Page> pageClass = installedPages.get((Path) context.getAttribute(NEW_PATH_KEY));
		Page page = null;
		Constructor<? extends Page> constructor = pageClass.getConstructor(Context.class);
		page = constructor.newInstance(context);
		if(page == null) {
			throw new IOException();								
		} else {
			context.setAttribute(EXECUTABLE, page);
		}
		return page;
	}
	enum Phase {
		IDENTIFICATION,
		APPLY,
		EXECUTION,
		BRANCH,
		ENCODING,
		FINALIZING,
		COMPLETE
	}

}
