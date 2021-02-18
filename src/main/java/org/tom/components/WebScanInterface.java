package org.tom.components;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.fasterxml.jackson.databind.*;

public class WebScanInterface {

  private static final String POST = "post";

  private static final String GET = "get";

  private static final String SAVE_LINKS = "/saveLinks";

  private static final String READ_PAGE = "/readLinks";

  private static final String PAGE_ADDRESS = "pageAddress";

 private static final String UNDERSCORE = "_";

  private static final String SPACE = " ";

  private static final String QUOTE = "\"";

  private static final String NULL_STRING = "";

  private static final String SESSION_KEY = "sessionKey";

  private static final String LINE_FEED = "\n";

  private static final String END = "/";

  private static final char OPENER = '<';

  private static final String NULL_KEY = NULL_STRING;

  private static final String SPAN_KEY = "span";

  private static final String LINK_KEY = "href";

  private static final String IMAGE_KEY = "image";

  private static final String CHECKBOX = "checkbox";

  private ObjectMapper mapper = new ObjectMapper();

  private Map<UUID, Session> sessions = new HashMap<>();

  class Session {
    private UUID key;
    private List<Record> links = new ArrayList<>();
    private Set<Integer> filter = new HashSet<>();
    public UUID getKey() {
      return key;
    }

    public Session(UUID things) {
      super();
      this.key = things;
    }

    public boolean getFilter(Integer key) {
      return !filter.contains(key);
    }

    public void setFilter(Integer key) {
      filter.add(key);
    }

    public void addLink(Record data) {
      links.add(data);
    }

    public boolean hasLink(Record data) {
      return links.contains(data);
    }

    public List<Record> getLinks() {
      return links;
    }
  }

 

  public String readLinks(String pageAddress, Map<String, String> filtered) {

    UUID sessionKey = filtered.get(SESSION_KEY) == null ? UUID.randomUUID() : UUID.fromString(filtered.get(SESSION_KEY));

    Session session = ensureSession(sessionKey);

    System.out.printf("\n reading %s", pageAddress);

    String result = null;

    try {

//      result = restTemplate.getForObject("http://" + pageAddress, String.class);

    } catch (Exception e) {

      System.out.println("e:"+e.getMessage());

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

        if (((data != null) && session.getFilter(data.getLink().hashCode())) && !session.hasLink(data)) {

          session.addLink(data);

        }

      }

    }

    return addLinksPage(session);

  }

 

  public Session ensureSession(UUID sessionKey) {

    Session session = sessions.get(sessionKey);

    if (session == null) {

      sessionKey = UUID.randomUUID();

      session = new Session(sessionKey);

      sessions.put(sessionKey, session);

    }

    return session;

  }

 

  public String saveLinks(Map<String, String> filtered) {

    String sessionKey = filtered.get(SESSION_KEY);

    Session session = sessions.get(UUID.fromString(sessionKey));

    if(session != null) {

      filtered.keySet().stream().filter(name -> nonLinkFitler(name)).forEach((key) -> session.setFilter(Integer.parseInt(key)));

    }

    return addLinksPage(session);

  }

 

public boolean nonLinkFitler(String name) {

                boolean result = true;

                result = !SESSION_KEY.equals(name);

                result &= !PAGE_ADDRESS.equals(name);

                return result;

}

 

  public String addLinksPage(Session session) {

    View page = new View();

    Body body = new Body();

    page.addContent(body);

    body.addContent(addLinksForm(session));

    StringBuilder sb = new StringBuilder();

    page.print(0,sb);

    return sb.toString();

  }

 

  private Form addLinksForm(Session session) {

    Form form = new Form();

    form.putAction(SAVE_LINKS);

    form.putMethod(POST);

    form.addContent(new Header("Links from page"));

    form.addContent(addHiddenInput(SESSION_KEY,session.getKey().toString()));

 

    ALink linkO = new ALink("New Page","/?"+SESSION_KEY+"="+session.getKey().toString());

    form.addContent(linkO);

 

    Table table = new Table();

    form.addContent(table);

    session.getLinks().stream().filter(link -> session.getFilter(link.getLink().hashCode())).map(link -> recordToRow(link)).reduce(table, (tableO, fdsa) -> (Table)tableO.addContent(fdsa), Table::add);

    table.addContent(addSubmitRow());

    return form;

  }

 

  public Row recordToRow(Record record) {

    Row row = new Row();

    Data d = new Data();

    Input input = new Input();

    input.setType(CHECKBOX);

    input.setName(NULL_STRING+record.getLink().hashCode());

    ALink linkO = new ALink(record.getText(),record.getLink());

    input.addContent(linkO);

    d.addContent(input);

    row.addContent(d);

    return row;

  }

 

  public String addStartPage(Map<String, String> data) {

    UUID sessionKey = null;

    if(data == null || data.get(SESSION_KEY) == null) {

      sessionKey = UUID.randomUUID();

    } else {

      sessionKey = UUID.fromString(data.get(SESSION_KEY));

    }

    sessionKey = ensureSession(sessionKey).getKey();

    View page = new View();

    Body body = new Body();

    page.addContent(body);

    body.addContent(addreadLinksForm(sessionKey.toString()));

    StringBuilder sb = new StringBuilder();

    page.print(0,sb);

    return sb.toString();

  }

 

  private Form addreadLinksForm(String sessionKey) {

    Form form = new Form();

    form.putMethod(GET);

    form.putAction(READ_PAGE);

    form.addContent(new Header("Pick a Page"));

    form.addContent(addHiddenInput(SESSION_KEY,sessionKey));

    Table table = new Table();

    form.addContent(table);

    table.addContent(addInputRow("Page Address       ", PAGE_ADDRESS));

    table.addContent(addSubmitRow());

    return form;

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

 

  private Row addSubmitRow() {

    Row row = new Row();

    Data d = new Data();

    Button submit = new Button("Submit");

    submit.setType("submit");

    d.addContent(submit);

    row.addContent(d);

    return row;

  }

 

  private Row addTextAreaRow(String text) {

    Row r = new Row();

    Data d = new Data();

    TextArea textArea = new TextArea();

    textArea.setName(text);

    textArea.addContent(new TextContent(text));

    d.addContent(textArea);

    r.addContent(d);

    return r;

  }

 

  private Row addInputRow(String label, String iname) {

    Row row = new Row();

    row.addContent(new Data(label));

    Data d = new Data();

    Input input = new Input();

    input.setType("text");

    input.setName(iname);

    d.addContent(input);

    row.addContent(d);

    return row;

  }

 

  private Input addHiddenInput(String iname,String value) {

    Input input = new Input();

    input.setType("hidden");

    input.setName(iname);

    input.setParameter("value", value);

    return input;

  }

 

  private Row addDoubleInputRow(String labelA, String inameA, String labelB, String inameB) {

    Row row = new Row();

    row.addContent(new Data(labelA));

    Data d = new Data();

    Input input = new Input();

    input.setType("text");

    input.setName(inameA);

    d.addContent(input);

    row.addContent(d);

    row.addContent(new Data(labelB));

    Data dB = new Data();

    Input inputB = new Input();

    inputB.setType("text");

    inputB.setName(inameB);

    dB.addContent(inputB);

    row.addContent(dB);

    return row;

  }

}