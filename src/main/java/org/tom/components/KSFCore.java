package org.tom.components;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tom.application.*;
import org.tom.servlet.*;

import static org.tom.servlet.Constants.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KSFCore {
	@Override
	public String toString() {
		return "KSFCore:"+id;
	}
	private String id;
	public KSFCore(String id) {
		super();
		this.id = id;
	}

	private static ObjectMapper mapper = new ObjectMapper();

	public String readLinks(Context session, String pageAddress) {
		System.out.printf("\n reading %s \n", pageAddress);
		String result = null;
		try {
			result = SSLSocketClientWithClientAuth.getForObject("localhost", 843, "faces");
//			result = new RestTemplateBuilder().build().getForObject("http://" + pageAddress, String.class);
//			if (result == null) {
//				result = new RestTemplateBuilder().build().getForObject("https://" + pageAddress, String.class);
//			}
		} catch (Exception e) {
			System.out.println("e:" + e.getMessage());
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
				if (((data != null) && !getFilteredFromSession(session, data))
						&& !getLinksFromSession(session).contains(data)) {
					getLinksFromSession(session).add(data);
				}
			}
		}
		return addLinksPage(session);
	}

	public String addLinksPage(Context session) {
		View page = new View();
		page.addContent(new Head(new JSFunctions()));
		Body body = new Body();
		page.addContent(body);
		body.addContent(addLinksForm(session));
		StringBuilder sb = new StringBuilder();
		page.print(0, sb);
		return sb.toString();
	}

	public Form addLinksForm(Context session) {
		Form form = new Form();
		form.putAction(SAVE_LINKS);
		form.putMethod(POST);
		form.addContent(new Header("Links from page"));
		form.addContent(addHiddenInput(SESSION_KEY, (String) session.getAttribute(JSESSIONID)));
		ALink linkO = new ALink("New Page", "/?" + SESSION_KEY + "=" + session.getAttribute(JSESSIONID));
		form.addContent(linkO);
		Table table = new Table();
		form.addContent(table);
		getLinksFromSession(session).stream().filter(record -> !getFilteredFromSession(session, record))
				.map(link -> recordToRow(link))
				.reduce(table, (tableO, fdsa) -> (Table) tableO.addContent(fdsa), Table::add);
		table.addContent(addSubmitRow("submit"));
		table.addContent(addResetRow("reset"));
		table.addContent(addButtonRow("button"));
		return form;
	}

	@SuppressWarnings("unchecked") 
	public static boolean getFilteredFromSession(Context session, Record record) {
		return ((Set<Integer>) session.getAttribute(FILTER)).contains(record.getLink().hashCode());
	}

	@SuppressWarnings("unchecked")
	public static List<Record> getLinksFromSession(Context session) {
		return (List<Record>) session.getAttribute(LINKS);
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

	public static Row addSubmitRow(String id) {
		Row row = new Row();
		Data d = new Data();
		Button submit = new Button(SUBMIT);
		submit.setId(id);
		submit.setType(SUBMIT);
		d.addContent(submit);
		row.addContent(d);
		return row;
	}

	public static Row addResetRow(String id) {
		Row row = new Row();
		Data d = new Data();
		Button submit = new Button(RESET);
		submit.setId(id);
		submit.setType(RESET);
		submit.setParameter("onClick", "Tom.demoReset()");
		d.addContent(submit);
		row.addContent(d);
		return row;
	}

	public static Row addButtonRow(String id) {
		Row row = new Row();
		Data d = new Data();
		Button submit = new Button("Button");
		submit.setId(id);
		submit.setType("button");
		submit.setParameter("onClick", "Tom.demoRequest()");
		d.addContent(submit);
		row.addContent(d);
		return row;
	}

	private Row addTextAreaRow(String text) {
		Row r = new Row();
		Data d = new Data();
		TextArea textArea = new TextArea();
		textArea.setId(text);
		textArea.setName(text);
		textArea.addContent(new TextContent(text));
		d.addContent(textArea);
		r.addContent(d);
		return r;
	}

	public static Row addInputRow(String label, String iname) {
		Row row = new Row();
		row.addContent(new Data(label));
		Data d = new Data();
		Input input = new Input();
		input.setType("text");
		input.setId(iname);
		input.setName(iname);
		d.addContent(input);
		row.addContent(d);
		return row;
	}

	public static Input addHiddenInput(String iname, String value) {
		Input input = new Input();
		input.setId(iname);
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

class JSFunctions extends Element {
	public JSFunctions() {
		super("script", false);
		addContent(new TextContent(functionsO));
	}
	private String functionsO = "(function(swindow)"
			+ "{"
			+ "	if(swindow.Tom==null){"
			+ "		swindow.Tom={};"
			+ "	}"
			+ "	tom = swindow.Tom;"
			+ "	var dataKey = \"Text\";"
			+ "	var selection = null;"
			+ "	var counter = 0;"
			+ "	var poppee = null;"
			+ ""
			+ "	tom.getHttpRequest = function()"
			+ "	{"
			+ "		var xmlhttp;"
			+ "		if (window.XMLHttpRequest) {"
			+ "			xmlhttp=new XMLHttpRequest();"
			+ "		} else {"
			+ "			xmlhttp=new ActiveXObject(\"Microsoft.XMLHTTP\");"
			+ "		}"
			+ "		return xmlhttp;"
			+ "	};"
			+ "	tom.demoReset = function ()"
			+ "	{"
			+ "		var request = tom.getHttpRequest();"
			+ "		request.onreadystatechange=function() {"
			+ "       if (request.readyState == 4 && request.status == 200) {"
			+ "         request.onreadystatechange = true;"
			+ "         var parser = new DOMParser();"
			+ "		    var xmlDoc = parser.parseFromString(request.responseText, \"text/html\");"
			+ "         var bodyObj = xmlDoc.getElementsByTagName(\"body\")[0];"
			+ "         if(bodyObj)"
			+ "         {"
			+ "           document.body.innerHTML = bodyObj.innerHTML;"
			+ "         }"
			+ "	      }"
			+ "		};"
			+ "		request.open(\"POST\",\"resetLinks\",true);"
			+ "		request.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded\");"
			+ "		request.send(\"action=takeNoAction\");"
			+ "	};"
			+ "	tom.demoRequest = function ()"
			+ "	{"
			+ "		var request = tom.getHttpRequest();"
			+ "		request.onreadystatechange=function() {"
			+ "       if (request.readyState == 4 && request.status == 200) {"
			+ "         request.onreadystatechange = true;"
			+ "         var parser = new DOMParser();"
			+ "		    var xmlDoc = parser.parseFromString(request.responseText, \"text/html\");"
			+ "         var bodyObj = xmlDoc.getElementsByTagName(\"body\")[0];"
			+ "         if(bodyObj)"
			+ "         {"
			+ "           document.body.innerHTML = bodyObj.innerHTML;"
			+ "         }"
			+ "	      }"
			+ "		};"
			+ "		request.open(\"POST\",\"demoUpdate\",true);"
			+ "		request.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded\");"
			+ "		request.send(\"action=takeNoAction\");"
			+ "	};"
			+ "})(window);";
	
	private String functions = "(function(swindow)"
			+ "{"
			+ "	if(swindow.Tom==null){"
			+ "		swindow.Tom={};"
			+ "	}"
			+ "	tom = swindow.Tom;"
			+ "	var dataKey = \"Text\";"
			+ "	var selection = null;"
			+ "	var counter = 0;"
			+ "	var poppee = null;"
			+ ""
			+ "	tom.getHttpRequest = function () "
			+ "	{"
			+ "		var xmlhttp;"
			+ "		if (window.XMLHttpRequest) {"
			+ "			xmlhttp=new XMLHttpRequest();"
			+ "		} else {// code for IE6, IE5"
			+ "			xmlhttp=new ActiveXObject(\"Microsoft.XMLHTTP\");"
			+ "		}"
			+ "		return xmlhttp;"
			+ "	};"
			+ "	tom.demoRequest = function () "
			+ "	{"
			+ "		var request = getHttpRequest();"
			+ "		request.onreadystatechange=function() {"
			+ "			if (request.readyState == 4 && request.status == 200) {"
			+ "				console.log(\"Response Received\");"
			+ "			}"
			+ "		};"
			+ "		request.open(\"POST\",\"post-handler.php\",true);"
			+ "		request.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded\");"
			+ "		req.xmlReq.setRequestHeader(\"Faces-Request\",\"partial/ajax\");"
			+ "		req.xmlReq.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded;charset=UTF-8\")"
			+ "		request.send(\"action=dosomething\");"
			+ "	};"
			+ "	//for(var i=0,len=methods.length;i<len;i++){try{returnVal=methods[i]()}catch(e){continue}return returnVal}throw new Error(\"Could not create an XHR object.\")};var $=function $(){var results=[],element;for(var i=0;i<arguments.length;i++){element=arguments[i];if(typeof element==\"string\"){element=document.getElementById(element)}results.push(element)}return results.length>1?results:results[0]};var getForm=function getForm(element){if(element){var form=$(element);while(form){if(form.nodeName&&(form.nodeName.toLowerCase()==\"form\")){return form}if(form.form){return form.form}if(form.parentNode){form=form.parentNode}else{form=null}}return document.forms[0]}return null};var getFormForId=function getFormForId(id){if(id){var node=document.getElementById(id);while(node){if(node.nodeName&&(node.nodeName.toLowerCase()==\"form\")){return node}if(node.form){return node.form}if(node.parentNode){node=node.parentNode}else{node=null}}}return null};var isInArray=function isInArray(array,value){for(var i=0;i<array.length;i++){if(array[i]===value){return true}}return false};var globalEval=function globalEval(src){if(window.execScript){window.execScript(src);return}var fn=function(){window.eval.call(window,src)};fn()};var stripScripts=function stripScripts(str){var findscripts=/<script[^>]*>([\\S\\s]*?)<\\/script>/igm;var findscript=/<script([^>]*)>([\\S\\s]*?)<\\/script>/im;var stripStart=/^\\s*(<!--)*\\s*(\\/\\/)*\\s*(\\/\\*)*\\s*\\n*\\**\\n*\\s*\\*.*\\n*\\s*\\*\\/(<!\\[CDATA\\[)*/;var findsrc=/src=\"([\\S]*?)\"/im;var findtype=/type=\"([\\S]*?)\"/im;var initialnodes=[];var scripts=[];"
			+ "	//initialnodes=str.match(findscripts);while(!!initialnodes&&initialnodes.length>0){var scriptStr=[];scriptStr=initialnodes.shift().match(findscript);var type=[];type=scriptStr[1].match(findtype);if(!!type&&type[1]){if(type[1]!==\"text/javascript\"){continue}}var src=[];src=scriptStr[1].match(findsrc);var script;if(!!src&&src[1]){var url=src[1];if(/\\/javax.faces.resource\\/jsf.js\\?ln=javax\\.faces/.test(url)){script=false}else{script=loadScript(url)}}else{if(!!scriptStr&&scriptStr[2]){script=scriptStr[2].replace(stripStart,\"\")}else{script=false}}if(!!script){scripts.push(script)}}return scripts};"
			+ "	//var loadScript=function loadScript(url)"
			+ "//	{"
			+ "		var xhr=getTransport();"
			+ "		if(xhr===null){"
			+ "			return\"\""
			+ "		}"
			+ "		xhr.open(\"GET\",url,false);"
			+ "		xhr.setRequestHeader(\"Content-Type\",\"application/x-javascript\");xhr.send(null);if(xhr.readyState==4&&xhr.status==200){return xhr.responseText}return\"\"};var runScripts=function runScripts(scripts){if(!scripts||scripts.length===0){return}var head=document.getElementsByTagName(\"head\")[0]||document.documentElement;while(scripts.length){var scriptNode=document.createElement(\"script\");scriptNode.type=\"text/javascript\";scriptNode.text=scripts.shift();head.appendChild(scriptNode);head.removeChild(scriptNode)}};var elementReplaceStr=function elementReplaceStr(element,tempTagName,src){var temp=document.createElement(tempTagName);if(element.id){temp.id=element.id}if(element.nodeName.toLowerCase()===\"head\"){throw new Error(\"Attempted to replace a head element - this is not allowed.\")}else{var scripts=[];if(isAutoExec()){temp.innerHTML=src}else{scripts=stripScripts(src);src=src.replace(/<script[^>]*type=\"text\\/javascript\"*>([\\S\\s]*?)<\\/script>/igm,\"\");temp.innerHTML=src}}replaceNode(temp,element);cloneAttributes(temp,element);runScripts(scripts)};var getText=function getText(oNode,deep){var Node={ELEMENT_NODE:1,ATTRIBUTE_NODE:2,TEXT_NODE:3,CDATA_SECTION_NODE:4,ENTITY_REFERENCE_NODE:5,ENTITY_NODE:6,PROCESSING_INSTRUCTION_NODE:7,COMMENT_NODE:8,DOCUMENT_NODE:9,DOCUMENT_TYPE_NODE:10,DOCUMENT_FRAGMENT_NODE:11,NOTATION_NODE:12};var s=\"\";var nodes=oNode.childNodes;for(var i=0;i<nodes.length;i++){var node=nodes[i];var nodeType=node.nodeType;if(nodeType==Node.TEXT_NODE||nodeType==Node.CDATA_SECTION_NODE){s+=node.data}else{if(deep===true&&(nodeType==Node.ELEMENT_NODE||nodeType==Node.DOCUMENT_NODE||nodeType==Node.DOCUMENT_FRAGMENT_NODE)){s+=getText(node,true)}}}return s};var PARSED_OK=\"Document contains no parsing errors\";var PARSED_EMPTY=\"Document is empty\";var PARSED_UNKNOWN_ERROR=\"Not well-formed or other error\";var getParseErrorText;if(isIE()){getParseErrorText=function(oDoc){var parseErrorText=PARSED_OK;if(oDoc&&oDoc.parseError&&oDoc.parseError.errorCode&&oDoc.parseError.errorCode!==0){parseErrorText=\"XML Parsing Error: \"+oDoc.parseError.reason+\"\\nLocation: \"+oDoc.parseError.url+\"\\nLine Number \"+oDoc.parseError.line+\", Column \"+oDoc.parseError.linepos+\":\\n\"+oDoc.parseError.srcText+\"\\n\";for(var i=0;i<oDoc.parseError.linepos;i++){parseErrorText+=\"-\"}parseErrorText+=\"^\\n\"}else{if(oDoc.documentElement===null){parseErrorText=PARSED_EMPTY}}return parseErrorText}}else{getParseErrorText=function(oDoc){var parseErrorText=PARSED_OK;if((!oDoc)||(!oDoc.documentElement)){parseErrorText=PARSED_EMPTY}else{if(oDoc.documentElement.tagName==\"parsererror\"){parseErrorText=oDoc.documentElement.firstChild.data;parseErrorText+=\"\\n\"+oDoc.documentElement.firstChild.nextSibling.firstChild.data}else{if(oDoc.getElementsByTagName(\"parsererror\").length>0){var parsererror=oDoc.getElementsByTagName(\"parsererror\")[0];parseErrorText=getText(parsererror,true)+\"\\n\"}else{if(oDoc.parseError&&oDoc.parseError.errorCode!==0){parseErrorText=PARSED_UNKNOWN_ERROR}}}}return parseErrorText}}if((typeof(document.importNode)==\"undefined\")&&isIE()){try{document.importNode=function(oNode,bChildren){var tmp;if(oNode.nodeName==\"#text\"){return document.createTextNode(oNode.data)}else{if(oNode.nodeName==\"tbody\"||oNode.nodeName==\"tr\"){tmp=document.createElement(\"table\")}else{if(oNode.nodeName==\"td\"){tmp=document.createElement(\"tr\")}else{if(oNode.nodeName==\"option\"){tmp=document.createElement(\"select\")}else{tmp=document.createElement(\"div\")}}}if(bChildren){tmp.innerHTML=oNode.xml?oNode.xml:oNode.outerHTML}else{tmp.innerHTML=oNode.xml?oNode.cloneNode(false).xml:oNode.cloneNode(false).outerHTML}return tmp.getElementsByTagName(\"*\")[0]}}}catch(e){}}var Node={ELEMENT_NODE:1,ATTRIBUTE_NODE:2,TEXT_NODE:3,CDATA_SECTION_NODE:4,ENTITY_REFERENCE_NODE:5,ENTITY_NODE:6,PROCESSING_INSTRUCTION_NODE:7,COMMENT_NODE:8,DOCUMENT_NODE:9,DOCUMENT_TYPE_NODE:10,DOCUMENT_FRAGMENT_NODE:11,NOTATION_NODE:12};var clearEvents=function clearEvents(node){if(!node){return}if(node.nodeType==Node.TEXT_NODE||node.nodeType==Node.COMMENT_NODE){return}var events=[\"abort\",\"blur\",\"change\",\"error\",\"focus\",\"load\",\"reset\",\"resize\",\"scroll\",\"select\",\"submit\",\"unload\",\"keydown\",\"keypress\",\"keyup\",\"click\",\"mousedown\",\"mousemove\",\"mouseout\",\"mouseover\",\"mouseup\",\"dblclick\"];try{for(var e in events){if(events.hasOwnProperty(e)){node[e]=null}}}catch(ex){}};var isIE9Plus=function isIE9Plus(){var iev=getIEVersion();if(iev>=9){return true}else{return false}};var deleteNode=function deleteNode(node){if(!node){return}if(!node.parentNode){return}if(!isIE()||(isIE()&&isIE9Plus())){node.parentNode.removeChild(node);return}if(node.nodeName.toLowerCase()===\"body\"){deleteChildren(node);try{node.outerHTML=\"\"}catch(ex){}return}var temp=node.ownerDocument.createElement(\"div\");var parent=node.parentNode;temp.appendChild(parent.removeChild(node));try{temp.outerHTML=\"\"}catch(ex){}};var deleteChildren=function deleteChildren(node){if(!node){return}for(var x=node.childNodes.length-1;x>=0;x--){var childNode=node.childNodes[x];deleteNode(childNode)}};var copyChildNodes=function copyChildNodes(nodeFrom,nodeTo){if((!nodeFrom)||(!nodeTo)){throw\"Both source and destination nodes must be provided\"}deleteChildren(nodeTo);var nodes=nodeFrom.childNodes;if(nodeFrom.ownerDocument==nodeTo.ownerDocument){while(nodeFrom.firstChild){nodeTo.appendChild(nodeFrom.firstChild)}}else{var ownerDoc=nodeTo.nodeType==Node.DOCUMENT_NODE?nodeTo:nodeTo.ownerDocument;var i;if(typeof(ownerDoc.importNode)!=\"undefined\"){for(i=0;i<nodes.length;i++){nodeTo.appendChild(ownerDoc.importNode(nodes[i],true))}}else{for(i=0;i<nodes.length;i++){nodeTo.appendChild(nodes[i].cloneNode(true))}}}};var replaceNode=function replaceNode(newNode,node){if(isIE()){node.parentNode.insertBefore(newNode,node);deleteNode(node)}else{node.parentNode.replaceChild(newNode,node)}};var propertyToAttribute=function propertyToAttribute(name){if(name===\"className\"){return\"class\"}else{if(name===\"xmllang\"){return\"xml:lang\"}else{return name.toLowerCase()}}};var isFunctionNative=function isFunctionNative(func){return/^\\s*function[^{]+{\\s*\\[native code\\]\\s*}\\s*$/.test(String(func))};var detectAttributes=function detectAttributes(element){if(element.hasAttribute&&isFunctionNative(element.hasAttribute)){return function(name){return element.hasAttribute(name)}}else{try{element.getAttribute;var html=element.outerHTML;var startTag=html.match(/^<[^>]*>/)[0];return function(name){return startTag.indexOf(name+\"=\")>-1}}catch(ex){return function(name){return element.getAttribute(name)}}}};var cloneAttributes=function cloneAttributes(target,source){var coreElementProperties=[\"className\",\"title\",\"lang\",\"xmllang\"];var inputElementProperties=[\"name\",\"value\",\"size\",\"maxLength\",\"src\",\"alt\",\"useMap\",\"tabIndex\",\"accessKey\",\"accept\",\"type\"];var inputElementBooleanProperties=[\"checked\",\"disabled\",\"readOnly\"];var listenerNames=[\"onclick\",\"ondblclick\",\"onmousedown\",\"onmousemove\",\"onmouseout\",\"onmouseover\",\"onmouseup\",\"onkeydown\",\"onkeypress\",\"onkeyup\",\"onhelp\",\"onblur\",\"onfocus\",\"onchange\",\"onload\",\"onunload\",\"onabort\",\"onreset\",\"onselect\",\"onsubmit\"];var sourceAttributeDetector=detectAttributes(source);var targetAttributeDetector=detectAttributes(target);var isInputElement=target.nodeName.toLowerCase()===\"input\";var propertyNames=isInputElement?coreElementProperties.concat(inputElementProperties):coreElementProperties;var isXML=!source.ownerDocument.contentType||source.ownerDocument.contentType==\"text/xml\";for(var iIndex=0,iLength=propertyNames.length;iIndex<iLength;iIndex++){var propertyName=propertyNames[iIndex];var attributeName=propertyToAttribute(propertyName);if(sourceAttributeDetector(attributeName)){if(attributeName==\"class\"){if(isIE()&&(source.getAttribute(propertyName)===source[propertyName])){attributeName=propertyName}}var newValue=isXML?source.getAttribute(attributeName):source[propertyName];var oldValue=target[propertyName];if(oldValue!=newValue){target[propertyName]=newValue}}else{if(attributeName==\"value\"&&(target.type!=\"checkbox\"&&target.type!=\"radio\")){target[propertyName]=\"\"}target.removeAttribute(attributeName)}}var booleanPropertyNames=isInputElement?inputElementBooleanProperties:[];for(var jIndex=0,jLength=booleanPropertyNames.length;jIndex<jLength;jIndex++){var booleanPropertyName=booleanPropertyNames[jIndex];var newBooleanValue=source[booleanPropertyName];var oldBooleanValue=target[booleanPropertyName];if(oldBooleanValue!=newBooleanValue){target[booleanPropertyName]=newBooleanValue}}if(sourceAttributeDetector(\"style\")){var newStyle;var oldStyle;if(isIE()){newStyle=source.style.cssText;oldStyle=target.style.cssText;if(newStyle!=oldStyle){target.style.cssText=newStyle}}else{newStyle=source.getAttribute(\"style\");oldStyle=target.getAttribute(\"style\");if(newStyle!=oldStyle){target.setAttribute(\"style\",newStyle)}}}else{if(targetAttributeDetector(\"style\")){target.removeAttribute(\"style\")}}if(!isIE()&&source.dir!=target.dir){if(sourceAttributeDetector(\"dir\")){target.dir=source.dir}else{if(targetAttributeDetector(\"dir\")){target.dir=\"\"}}}for(var lIndex=0,lLength=listenerNames.length;lIndex<lLength;lIndex++){var name=listenerNames[lIndex];target[name]=source[name]?source[name]:null;if(source[name]){source[name]=null}}try{var targetDataset=target.dataset;var sourceDataset=source.dataset;if(targetDataset||sourceDataset){for(var tp in targetDataset){delete targetDataset[tp]}for(var sp in sourceDataset){targetDataset[sp]=sourceDataset[sp]}}}catch(ex){}};var elementReplace=function elementReplace(newElement,origElement){copyChildNodes(newElement,origElement);origElement.innerHTML=origElement.innerHTML;try{cloneAttributes(origElement,newElement)}catch(ex){if(jsf.getProjectStage()==\"Development\"){throw new Error(\"Error updating attributes\")}}deleteNode(newElement)};var getBodyElement=function getBodyElement(docStr){var doc;var body;if(typeof DOMParser!==\"undefined\"){doc=(new DOMParser()).parseFromString(docStr,\"text/xml\")}else{if(typeof ActiveXObject!==\"undefined\"){doc=new ActiveXObject(\"MSXML2.DOMDocument\");doc.loadXML(docStr)}else{throw new Error(\"You don't seem to be running a supported browser\")}}if(getParseErrorText(doc)!==PARSED_OK){throw new Error(getParseErrorText(doc))}body=doc.getElementsByTagName(\"body\")[0];if(!body){throw new Error(\"Can't find body tag in returned document.\")}return body};var getViewStateElement=function getViewStateElement(form){var viewStateElement=form[\"javax.faces.ViewState\"];if(viewStateElement){return viewStateElement}else{var formElements=form.elements;for(var i=0,length=formElements.length;i<length;i++){var formElement=formElements[i];if(formElement.name==\"javax.faces.ViewState\"){return formElement}}}return undefined};var doUpdate=function doUpdate(element,context){var id,content,markup,state;var stateForm;var scripts=[];id=element.getAttribute(\"id\");if(id===\"javax.faces.ViewState\"){state=element.firstChild;if(typeof context.formid!==\"undefined\"&&context.formid!==null){stateForm=getFormForId(context.formid)}else{stateForm=getFormForId(context.element.id)}if(!stateForm||!stateForm.elements){return}var field=getViewStateElement(stateForm);if(typeof field==\"undefined\"){field=document.createElement(\"input\");field.type=\"hidden\";field.name=\"javax.faces.ViewState\";stateForm.appendChild(field)}field.value=state.nodeValue;if(typeof context.render!==\"undefined\"&&context.render!==null){var temp=context.render.split(\" \");for(var i=0;i<temp.length;i++){if(temp.hasOwnProperty(i)){var f=document.forms[temp[i]];if(typeof f!==\"undefined\"&&f!==null&&f.id!==context.formid){field=getViewStateElement(f);if(typeof field===\"undefined\"){field=document.createElement(\"input\");field.type=\"hidden\";field.name=\"javax.faces.ViewState\";f.appendChild(field)}field.value=state.nodeValue}}}}return}markup=\"\";for(var j=0;j<element.childNodes.length;j++){content=element.childNodes[j];markup+=content.nodeValue}var src=markup;if(id===\"javax.faces.ViewRoot\"||id===\"javax.faces.ViewBody\"){var bodyStartEx=new RegExp(\"< *body[^>]*>\",\"gi\");var bodyEndEx=new RegExp(\"< */ *body[^>]*>\",\"gi\");var newsrc;var docBody=document.getElementsByTagName(\"body\")[0];var bodyStart=bodyStartEx.exec(src);if(bodyStart!==null){try{scripts=stripScripts(src);newsrc=src.replace(/<script[^>]*type=\"text\\/javascript\"*>([\\S\\s]*?)<\\/script>/igm,\"\");elementReplace(getBodyElement(newsrc),docBody);runScripts(scripts)}catch(e){var srcBody,bodyEnd;bodyEnd=bodyEndEx.exec(src);if(bodyEnd!==null){srcBody=src.substring(bodyStartEx.lastIndex,bodyEnd.index)}else{srcBody=src.substring(bodyStartEx.lastIndex)}elementReplaceStr(docBody,\"body\",srcBody)}}else{elementReplaceStr(docBody,\"body\",src)}}else{if(id===\"javax.faces.ViewHead\"){throw new Error(\"javax.faces.ViewHead not supported - browsers cannot reliably replace the head's contents\")}else{var d=$(id);if(!d){throw new Error(\"During update: \"+id+\" not found\")}var parent=d.parentNode;var html=src.replace(/^\\s+/g,\"\").replace(/\\s+$/g,\"\");var parserElement=document.createElement(\"div\");var tag=d.nodeName.toLowerCase();var tableElements=[\"td\",\"th\",\"tr\",\"tbody\",\"thead\",\"tfoot\"];var isInTable=false;for(var tei=0,tel=tableElements.length;tei<tel;tei++){if(tableElements[tei]==tag){isInTable=true;break}}if(isInTable){if(isAutoExec()){parserElement.innerHTML=\"<table>\"+html+\"</table>\"}else{scripts=stripScripts(html);html=html.replace(/<script[^>]*type=\"text\\/javascript\"*>([\\S\\s]*?)<\\/script>/igm,\"\");parserElement.innerHTML=\"<table>\"+html+\"</table>\"}var newElement=parserElement.firstChild;while((null!==newElement)&&(id!==newElement.id)){newElement=newElement.firstChild}parent.replaceChild(newElement,d);runScripts(scripts)}else{if(d.nodeName.toLowerCase()===\"input\"){parserElement=document.createElement(\"div\");parserElement.innerHTML=html;newElement=parserElement.firstChild;cloneAttributes(d,newElement);deleteNode(parserElement)}else{if(html.length>0){if(isAutoExec()){parserElement.innerHTML=html}else{scripts=stripScripts(html);html=html.replace(/<script[^>]*type=\"text\\/javascript\"*>([\\S\\s]*?)<\\/script>/igm,\"\");parserElement.innerHTML=html}replaceNode(parserElement.firstChild,d);deleteNode(parserElement);runScripts(scripts)}}}}}};var doDelete=function doDelete(element){var id=element.getAttribute(\"id\");var target=$(id);deleteNode(target)};var doInsert=function doInsert(element){var tablePattern=new RegExp(\"<\\\\s*(td|th|tr|tbody|thead|tfoot)\",\"i\");var scripts=[];var target=$(element.firstChild.getAttribute(\"id\"));var parent=target.parentNode;var html=element.firstChild.firstChild.nodeValue;var isInTable=tablePattern.test(html);if(!isAutoExec()){scripts=stripScripts(html);html=html.replace(/<script[^>]*type=\"text\\/javascript\"*>([\\S\\s]*?)<\\/script>/igm,\"\")}var tempElement=document.createElement(\"div\");var newElement=null;if(isInTable){tempElement.innerHTML=\"<table>\"+html+\"</table>\";newElement=tempElement.firstChild;while((null!==newElement)&&(\"\"==newElement.id)){newElement=newElement.firstChild}}else{tempElement.innerHTML=html;newElement=tempElement.firstChild}if(element.firstChild.nodeName===\"after\"){target=target.nextSibling}if(!!tempElement.innerHTML){parent.insertBefore(newElement,target)}runScripts(scripts);deleteNode(tempElement)};var doAttributes=function doAttributes(element){var id=element.getAttribute(\"id\");var target=$(id);if(!target){throw new Error(\"The specified id: \"+id+\" was not found in the page.\")}var nodes=element.childNodes;for(var i=0;i<nodes.length;i++){var name=nodes[i].getAttribute(\"name\");var value=nodes[i].getAttribute(\"value\");if(name===\"disabled\"){target.disabled=value===\"disabled\"||value===\"true\";return}else{if(name===\"checked\"){target.checked=value===\"checked\"||value===\"on\"||value===\"true\";return}else{if(name==\"readonly\"){target.readOnly=value===\"readonly\"||value===\"true\";return}}}if(!isIE()){if(name===\"value\"){target.value=value}else{target.setAttribute(name,value)}}else{if(name===\"class\"){target.className=value}else{if(name===\"for\"){name=\"htmlFor\";target.setAttribute(name,value,0)}else{if(name===\"style\"){target.style.setAttribute(\"cssText\",value,0)}else{if(name.substring(0,2)===\"on\"){var c=document.body.appendChild(document.createElement(\"span\"));try{c.innerHTML=\"<span \"+name+'=\"'+value+'\"/>';target[name]=c.firstChild[name]}finally{document.body.removeChild(c)}}else{if(name===\"dir\"){if(jsf.getProjectStage()==\"Development\"){throw new Error(\"Cannot set 'dir' attribute in IE\")}}else{target.setAttribute(name,value,0)}}}}}}}};var doEval=function doEval(element){var evalText=element.firstChild.nodeValue;globalEval(evalText)};var Queue=new function Queue(){var queue=[];var queueSpace=0;this.getSize=function getSize(){return queue.length-queueSpace};this.isEmpty=function isEmpty(){return(queue.length===0)};this.enqueue=function enqueue(element){queue.push(element)};this.dequeue=function dequeue(){var element=undefined;if(queue.length){element=queue[queueSpace];if(++queueSpace*2>=queue.length){queue=queue.slice(queueSpace);queueSpace=0}}try{return element}finally{element=null}};this.getOldestElement=function getOldestElement(){var element=undefined;if(queue.length){element=queue[queueSpace]}try{return element}finally{element=null}}}();var AjaxEngine=function AjaxEngine(context){var req={};req.url=null;req.context=context;req.context.sourceid=null;req.context.onerror=null;req.context.onevent=null;req.context.formid=null;req.xmlReq=null;req.async=true;req.parameters={};req.queryString=null;req.method=null;req.status=null;req.fromQueue=false;req.que=Queue;req.xmlReq=getTransport();if(req.xmlReq===null){return null}function noop(){}req.xmlReq.onreadystatechange=function(){if(req.xmlReq.readyState===4){req.onComplete();req.xmlReq.onreadystatechange=noop;req.xmlReq=null}};req.onComplete=function onComplete(){if(req.xmlReq.status&&(req.xmlReq.status>=200&&req.xmlReq.status<300)){sendEvent(req.xmlReq,req.context,\"complete\");jsf.ajax.response(req.xmlReq,req.context)}else{sendEvent(req.xmlReq,req.context,\"complete\");sendError(req.xmlReq,req.context,\"httpError\")}var nextReq=req.que.getOldestElement();if(nextReq===null||typeof nextReq===\"undefined\"){return}while((typeof nextReq.xmlReq!==\"undefined\"&&nextReq.xmlReq!==null)&&nextReq.xmlReq.readyState===4){req.que.dequeue();nextReq=req.que.getOldestElement();if(nextReq===null||typeof nextReq===\"undefined\"){break}}if(nextReq===null||typeof nextReq===\"undefined\"){return}if((typeof nextReq.xmlReq!==\"undefined\"&&nextReq.xmlReq!==null)&&nextReq.xmlReq.readyState===0){nextReq.fromQueue=true;nextReq.sendRequest()}};req.setupArguments=function(args){for(var i in args){if(args.hasOwnProperty(i)){if(typeof req[i]===\"undefined\"){req.parameters[i]=args[i]}else{req[i]=args[i]}}}};req.sendRequest=function(){if(req.xmlReq!==null){if(!req.que.isEmpty()){if(!req.fromQueue){req.que.enqueue(req);return}}if(!req.fromQueue){req.que.enqueue(req)}if(req.generateUniqueUrl&&req.method==\"GET\"){req.parameters.AjaxRequestUniqueId=new Date().getTime()+\"\"+req.requestIndex}var content=null;for(var i in req.parameters){if(req.parameters.hasOwnProperty(i)){if(req.queryString.length>0){req.queryString+=\"&\"}req.queryString+=encodeURIComponent(i)+\"=\"+encodeURIComponent(req.parameters[i])}}if(req.method===\"GET\"){if(req.queryString.length>0){req.url+=((req.url.indexOf(\"?\")>-1)?\"&\":\"?\")+req.queryString}}req.xmlReq.open(req.method,req.url,req.async);if(req.method===\"POST\"){if(typeof req.xmlReq.setRequestHeader!==\"undefined\"){req.xmlReq.setRequestHeader(\"Faces-Request\",\"partial/ajax\");req.xmlReq.setRequestHeader(\"Content-type\",\"application/x-www-form-urlencoded;charset=UTF-8\")}content=req.queryString}if(!req.async){req.xmlReq.onreadystatechange=null}sendEvent(req.xmlReq,req.context,\"begin\");req.xmlReq.send(content);if(!req.async){req.onComplete()}}};return req};var sendError=function sendError(request,context,status,description,serverErrorName,serverErrorMessage){var sent=false;var data={};data.type=\"error\";data.status=status;data.source=context.sourceid;data.responseCode=request.status;data.responseXML=request.responseXML;data.responseText=request.responseText;if(typeof data.source===\"string\"){data.source=document.getElementById(data.source)}if(description){data.description=description}else{if(status==\"httpError\"){if(data.responseCode===0){data.description=\"The Http Transport returned a 0 status code.  This is usually the result of mixing ajax and full requests.  This is usually undesired, for both performance and data integrity reasons.\"}else{data.description=\"There was an error communicating with the server, status: \"+data.responseCode}}else{if(status==\"serverError\"){data.description=serverErrorMessage}else{if(status==\"emptyResponse\"){data.description=\"An empty response was received from the server.  Check server error logs.\"}else{if(status==\"malformedXML\"){if(getParseErrorText(data.responseXML)!==PARSED_OK){data.description=getParseErrorText(data.responseXML)}else{data.description=\"An invalid XML response was received from the server.\"}}}}}}if(status==\"serverError\"){data.errorName=serverErrorName;data.errorMessage=serverErrorMessage}if(context.onerror){context.onerror.call(null,data);sent=true}for(var i in errorListeners){if(errorListeners.hasOwnProperty(i)){errorListeners[i].call(null,data);sent=true}}if(!sent&&jsf.getProjectStage()===\"Development\"){if(status==\"serverError\"){alert(\"serverError: \"+serverErrorName+\" \"+serverErrorMessage)}else{alert(status+\": \"+data.description)}}};var sendEvent=function sendEvent(request,context,status){var data={};data.type=\"event\";data.status=status;data.source=context.sourceid;if(typeof data.source===\"string\"){data.source=document.getElementById(data.source)}if(status!==\"begin\"){data.responseCode=request.status;data.responseXML=request.responseXML;data.responseText=request.responseText}if(context.onevent){context.onevent.call(null,data)}for(var i in eventListeners){if(eventListeners.hasOwnProperty(i)){eventListeners[i].call(null,data)}}};return{addOnError:function addOnError(callback){if(typeof callback===\"function\"){errorListeners[errorListeners.length]=callback}else{throw new Error(\"jsf.ajax.addOnError:  Added a callback that was not a function.\")}},addOnEvent:function addOnEvent(callback){if(typeof callback===\"function\"){eventListeners[eventListeners.length]=callback}else{throw new Error(\"jsf.ajax.addOnEvent: Added a callback that was not a function\")}},request:function request(source,event,options){var element,form;var all,none;var context={};if(typeof source===\"undefined\"||source===null){throw new Error(\"jsf.ajax.request: source not set\")}if(typeof source===\"string\"){element=document.getElementById(source)}else{if(typeof source===\"object\"){element=source}else{throw new Error(\"jsf.request: source must be object or string\")}}if(!element.name){element.name=element.id}context.element=element;if(typeof(options)===\"undefined\"||options===null){options={}}var onerror=false;if(options.onerror&&typeof options.onerror===\"function\"){onerror=options.onerror}else{if(options.onerror&&typeof options.onerror!==\"function\"){throw new Error(\"jsf.ajax.request: Added an onerror callback that was not a function\")}}var onevent=false;if(options.onevent&&typeof options.onevent===\"function\"){onevent=options.onevent}else{if(options.onevent&&typeof options.onevent!==\"function\"){throw new Error(\"jsf.ajax.request: Added an onevent callback that was not a function\")}}form=getForm(element);if(!form){throw new Error(\"jsf.ajax.request: Method must be called within a form\")}var viewState=jsf.getViewState(form);var args={};args[\"javax.faces.source\"]=element.id;if(event&&!!event.type){args[\"javax.faces.partial.event\"]=event.type}if(options.execute){none=options.execute.search(/@none/);if(none<0){all=options.execute.search(/@all/);if(all<0){options.execute=options.execute.replace(\"@this\",element.id);options.execute=options.execute.replace(\"@form\",form.id);var temp=options.execute.split(\" \");if(!isInArray(temp,element.name)){options.execute=element.name+\" \"+options.execute}}else{options.execute=\"@all\"}args[\"javax.faces.partial.execute\"]=options.execute}}else{options.execute=element.name+\" \"+element.id;args[\"javax.faces.partial.execute\"]=options.execute}if(options.render){none=options.render.search(/@none/);if(none<0){all=options.render.search(/@all/);if(all<0){options.render=options.render.replace(\"@this\",element.id);options.render=options.render.replace(\"@form\",form.id)}else{options.render=\"@all\"}args[\"javax.faces.partial.render\"]=options.render}}delete options.execute;delete options.render;delete options.onerror;delete options.onevent;for(var property in options){if(options.hasOwnProperty(property)){args[property]=options[property]}}args[\"javax.faces.partial.ajax\"]=\"true\";args.method=\"POST\";var encodedUrlField=form.elements[\"javax.faces.encodedURL\"];if(typeof encodedUrlField==\"undefined\"){args.url=form.action}else{args.url=encodedUrlField.value}var ajaxEngine=new AjaxEngine(context);ajaxEngine.setupArguments(args);ajaxEngine.queryString=viewState;ajaxEngine.context.onevent=onevent;ajaxEngine.context.onerror=onerror;ajaxEngine.context.sourceid=element.id;ajaxEngine.context.formid=form.id;ajaxEngine.context.render=args[\"javax.faces.partial.render\"];ajaxEngine.sendRequest();element=null;form=null},response:function response(request,context){if(!request){throw new Error(\"jsf.ajax.response: Request parameter is unset\")}if(typeof context.sourceid===\"string\"){context.sourceid=document.getElementById(context.sourceid)}var xml=request.responseXML;if(xml===null){sendError(request,context,\"emptyResponse\");return}if(getParseErrorText(xml)!==PARSED_OK){sendError(request,context,\"malformedXML\");return}var responseType=xml.getElementsByTagName(\"partial-response\")[0].firstChild;if(responseType.nodeName===\"error\"){var errorName=\"\";var errorMessage=\"\";var element=responseType.firstChild;if(element.nodeName===\"error-name\"){if(null!=element.firstChild){errorName=element.firstChild.nodeValue}}element=responseType.firstChild.nextSibling;if(element.nodeName===\"error-message\"){if(null!=element.firstChild){errorMessage=element.firstChild.nodeValue}}sendError(request,context,\"serverError\",null,errorName,errorMessage);sendEvent(request,context,\"success\");return}if(responseType.nodeName===\"redirect\"){window.location=responseType.getAttribute(\"url\");return}if(responseType.nodeName!==\"changes\"){sendError(request,context,\"malformedXML\",\"Top level node must be one of: changes, redirect, error, received: \"+responseType.nodeName+\" instead.\");return}var changes=responseType.childNodes;try{for(var i=0;i<changes.length;i++){switch(changes[i].nodeName){case\"update\":doUpdate(changes[i],context);break;case\"delete\":doDelete(changes[i]);break;case\"insert\":doInsert(changes[i]);break;case\"attributes\":doAttributes(changes[i]);break;case\"eval\":doEval(changes[i]);break;case\"extension\":break;default:sendError(request,context,\"malformedXML\",\"Changes allowed are: update, delete, insert, attributes, eval, extension.  Received \"+changes[i].nodeName+\" instead.\");return}}}catch(ex){sendError(request,context,\"malformedXML\",ex.message);return}sendEvent(request,context,\"success\")}}}();jsf.getProjectStage=function(){if(typeof mojarra!==\"undefined\"&&typeof mojarra.projectStageCache!==\"undefined\"){return mojarra.projectStageCache}var a=document.getElementsByTagName(\"script\");var b;var e=0;var d;var c;while(e<a.length){if(typeof a[e].src===\"string\"&&a[e].src.match(\"/javax.faces.resource/jsf.js?.*ln=javax.faces\")){b=a[e].src;break}e++}if(typeof b==\"string\"){c=b.match(\"stage=(.*)\");if(c){d=c[1]}}if(typeof d===\"undefined\"||!d){d=\"Production\"}mojarra=mojarra||{};mojarra.projectStageCache=d;return mojarra.projectStageCache};jsf.getViewState=function(b){if(!b){throw new Error(\"jsf.getViewState:  form must be set\")}var d=b.elements;var g=d.length;var k=[];var a=function(j,l){var i=\"\";if(k.length>0){i=\"&\"}i+=encodeURIComponent(j)+\"=\"+encodeURIComponent(l);k.push(i)};for(var f=0;f<g;f++){var c=d[f];if(c.name===\"\"){continue}if(!c.disabled){switch(c.type){case\"submit\":break;case\"image\":break;case\"select-one\":if(c.selectedIndex>=0){a(c.name,c.options[c.selectedIndex].value)}break;case\"select-multiple\":for(var e=0;e<c.options.length;e++){if(c.options[e].selected){a(c.name,c.options[e].value)}}break;case\"checkbox\":case\"radio\":if(c.checked){a(c.name,c.value||\"on\")}break;default:var h=c.nodeName.toLowerCase();if(h===\"input\"||h===\"select\"||h===\"button\"||h===\"object\"||h===\"textarea\"){a(c.name,c.value)}break}}}return k.join(\"\")};jsf.util={};jsf.util.chain=function(g,d){if(arguments.length<3){return true}var a=(typeof g===\"object\")?g:null;for(var b=2;b<arguments.length;b++){var e=new Function(\"event\",arguments[b]);var c=e.call(a,d);if(c===false){return false}}return true};jsf.specversion=20000;jsf.implversion=3}if(typeof OpenAjax!==\"undefined\"&&typeof OpenAjax.hub.registerLibrary!==\"undefined\"){OpenAjax.hub.registerLibrary(\"mojarra\",\"www.sun.com\",\"1.0\",null)}var mojarra=mojarra||{};mojarra.dpf=function dpf(c){var b=c.adp;if(b!==null){for(var a=0;a<b.length;a++){c.removeChild(b[a])}}};mojarra.apf=function apf(e,c){var d=new Array();e.adp=d;var b=0;for(var a in c){if(c.hasOwnProperty(a)){var g=document.createElement(\"input\");g.type=\"hidden\";g.name=a;g.value=c[a];e.appendChild(g);d[b++]=g}}};mojarra.jsfcljs=function jsfcljs(c,b,a){mojarra.apf(c,b);var d=c.target;if(a){c.target=a}c.submit();c.target=d;mojarra.dpf(c)};mojarra.jsfcbk=function jsfcbk(b,a,c){return b.call(a,c)};mojarra.ab=function ab(c,d,g,a,b,f){if(!f){f={}}if(g){f[\"javax.faces.behavior.event\"]=g}if(a){f.execute=a}if(b){f.render=b}jsf.ajax.request(c,d,f)};"
			+ "	tom.subPop = function(form)"
			+ "	{"
			+ "		var data = null;"
			+ "		if(form != null)"
			+ "		{"
			+ "			data = tom.GetMessageBody (form);	"
			+ "		}"
			+ "		"
			+ "		tom.AjaxSend(data, 'servlet/Cars', 'post', true);"
			+ "		"
			+ "		return false;"
			+ "	};"
			+ "	tom.ajaxSubmit = function(eevent,callback)"
			+ "		{"
			+ "			eevent.preventDefault();"
			+ "			var form = eevent.target.parentNode;"
			+ "			if (!(form instanceof HTMLFormElement)) {"
			+ "				form = form.parentNode;"
			+ "				if (!(form instanceof HTMLFormElement)) {"
			+ "					return;"
			+ "				}"
			+ "			}"
			+ "			var xhr = new XMLHttpRequest();"
			+ "			var fd = new FormData();"
			+ "			for (var i = 0; i < form.elements.length; i++) {"
			+ "				var elem = form.elements[i];"
			+ "				var type = elem.nodeName.toLowerCase();"
			+ "				var kind = elem.type.toLowerCase();"
			+ "				if (type == 'input' && kind == 'file') {"
			+ "					var gile = elem.files[0];"
			+ "					var id = elem.id;"
			+ "					fd.append( id, gile );"
			+ "				}"
			+ "				else if (type == 'input' && kind != 'submit' ) {"
			+ "					var name = elem.name;"
			+ "					if (name) {"
			+ "						var value = elem.value;"
			+ "						fd.append( name, value );"
			+ "					}"
			+ "					else"
			+ "					{"
			+ "						var id = elem.id;"
			+ "						var value = elem.value;"
			+ "						fd.append( id, value );"
			+ "					}"
			+ "				}"
			+ "			}"
			+ "			xhr.onreadystatechange = function () {tom.OnReadyStateChanged (xhr, form); };"
			+ "			xhr.open( 'post', 'servlet/Cars', true );"
			+ "			xhr.send( fd );"
			+ "			return false;"
			+ "		};"
			+ "	tom.popForm = function (sdocument,rect)"
			+ "	{"
			+ "		if(rect == null)"
			+ "		{"
			+ "			rect = sdocument.body.parentElement.getBoundingClientRect();"
			+ "		}"
			+ "		var nodeForm=sdocument.createElement(\"form\");"
			+ "		nodeForm.setAttribute(\"id\",\"floater_\" + counter);"
			+ "		nodeForm.setAttribute(\"enctype\",\"multipart/form-data/\");"
			+ "		nodeForm.setAttribute(\"method\",\"post\");"
			+ "		//"
			+ "		var nodeDiv=sdocument.createElement(\"div\");"
			+ "		nodeDiv.setAttribute(\"id\",\"Overlay_\" + counter);"
			+ "		nodeDiv.setAttribute(\"style\",\"position:absolute; background-color:white\");"
			+ "		nodeDiv.setAttribute(\"draggable\",\"true\");"
			+ "		nodeDiv.style.top    = rect.top + \"px\";"
			+ "		nodeDiv.style.left   = rect.left + \"px\";"
			+ "		nodeDiv.style.height = rect.height + \"px\";"
			+ "		nodeDiv.style.width  = rect.width + \"px\";"
			+ "		//"
			+ "		var nodeImg=sdocument.createElement(\"img\");"
			+ "		nodeImg.setAttribute(\"src\",\"Camry1987.jpg\");"
			+ "		nodeImg.setAttribute(\"height\",80 + \"px\");"
			+ "		nodeImg.setAttribute(\"width\",130 + \"px\");"
			+ "		nodeImg.setAttribute(\"id\",\"Imagine_\" + counter);"
			+ "		nodeDiv.appendChild(nodeImg);"
			+ "		//"
			+ "		var nodeInputH=sdocument.createElement(\"input\");"
			+ "		nodeInputH.setAttribute(\"value\",\"true\");"
			+ "		nodeInputH.setAttribute(\"type\",\"hidden\");"
			+ "		nodeInputH.setAttribute(\"id\",\"hideeP\");"
			+ "		nodeInputH.setAttribute(\"name\",\"javax.face.partial\");"
			+ "		nodeDiv.appendChild(nodeInputH);"
			+ "		//"
			+ "		var nodeInputH=sdocument.createElement(\"input\");"
			+ "		nodeInputH.setAttribute(\"value\",\"@#$%@#$%@#$%\");"
			+ "		nodeInputH.setAttribute(\"type\",\"hidden\");"
			+ "		nodeInputH.setAttribute(\"id\",\"hideeS\");"
			+ "		nodeInputH.setAttribute(\"name\",\"javax.face.viewstate\");"
			+ "		nodeDiv.appendChild(nodeInputH);"
			+ "		//"
			+ "		var nodeInputH=sdocument.createElement(\"input\");"
			+ "		nodeInputH.setAttribute(\"value\",\"submit\");"
			+ "		nodeInputH.setAttribute(\"type\",\"hidden\");"
			+ "		nodeInputH.setAttribute(\"id\",\"hideeB\");"
			+ "		nodeInputH.setAttribute(\"name\",\"javax.rich.source\");"
			+ "		nodeDiv.appendChild(nodeInputH);"
			+ "		//"
			+ "		var nodeInput=sdocument.createElement(\"input\");"
			+ "		nodeInput.setAttribute(\"value\",\"Add some text, why don't you\");"
			+ "		nodeInput.setAttribute(\"type\",\"text\");"
			+ "		nodeInput.setAttribute(\"id\",\"textA\");"
			+ "		nodeDiv.appendChild(nodeInput);"
			+ "		//"
			+ "		nodeInput=sdocument.createElement(\"input\");"
			+ "		nodeInput.setAttribute(\"value\",\"unknown\");"
			+ "		nodeInput.setAttribute(\"type\",\"file\");"
			+ "		nodeInput.setAttribute(\"id\",\"fileInput\");"
			+ "		nodeInput.setAttribute(\"name\",\"fileInput\");"
			+ "		nodeDiv.appendChild(nodeInput);"
			+ "		//"
			+ "		var nodeBtn=sdocument.createElement(\"input\");"
			+ "		nodeBtn.type  = \"submit\";"
			+ "		nodeBtn.value = \"Upload\";"
			+ "		nodeBtn.setAttribute(\"id\",\"submitter\");"
			+ "		nodeBtn.setAttribute(\"onclick\",\"tom.ajaxSubmit(event,tom.popOne)\");"
			+ "		nodeDiv.appendChild(nodeBtn);"
			+ "		//"
			+ "		var nodeTable=sdocument.createElement(\"table\");"
			+ "		nodeTable.setAttribute(\"id\",\"updatable\");"
			+ "		var nodeTbody=sdocument.createElement(\"tbody\");"
			+ "		nodeTable.appendChild(nodeTbody);"
			+ "		var nodeRow=sdocument.createElement(\"tr\");"
			+ "		nodeTbody.appendChild(nodeRow);"
			+ "		var nodeHead=sdocument.createElement(\"th\");"
			+ "		nodeHead.innerText = \"Table to be Updated\";"
			+ "		nodeRow.appendChild(nodeHead);"
			+ "		nodeDiv.appendChild(nodeTable);"
			+ "		//"
			+ "		nodeForm.appendChild(nodeDiv);"
			+ "		return nodeForm;"
			+ "	};"
			+ "	tom.popOne = function ()"
			+ "	{"
			+ "		var newDocument = this.xml;"
			+ "		poppee = swindow.open(null,\"poppee_\"+counter,\"height=100px,width=100px,menubar=1,statusbar=1\");"
			+ "		counter++;"
			+ "		if(poppee != null)"
			+ "		{"
			+ "			var nodeDiv = tom.popForm(poppee.document,null);"
			+ "			poppee.document.write(\"<!DOCTYPE html><html><body><h1>Poppy</h1></html></body>\");"
			+ "			poppee.document.body.appendChild(nodeDiv);"
			+ "		}"
			+ "	};"
			+ "	tom.closePop = function ()"
			+ "	{"
			+ "		poppee.close();"
			+ "	};"
			+ "	tom.tackOne = function ()"
			+ "	{"
			+ "		var targetDiv = document.getElementById(\"RowDiv_\" + cars);"
			+ "		if(targetDiv != null)"
			+ "		{"
			+ "			var rect = targetDiv.getBoundingClientRect();"
			+ "			var htmlRect = document.body.parentElement.getBoundingClientRect();"
			+ "			rect.top    = rect.top - htmlRect.top;"
			+ "			rect.left   = rect.left - htmlRect.left;"
			+ "			rect.height = rect.height*2;"
			+ "			rect.width  = rect.width;"
			+ "			//"
			+ "			var nodeDiv = tom.popForm(document,rect);"
			+ "			targetDiv.appendChild(nodeDiv);"
			+ "		}"
			+ "	};"
			+ "	tom.allowDrop = function (ev) {"
			+ "		ev.preventDefault();"
			+ "	};"
			+ "	tom.drag = function (ev) {"
			+ "		var sourceContent = ev.target;"
			+ "		while(!sourceContent.id.contains(\"RowDiv_\"))"
			+ "		{"
			+ "			sourceContent = sourceContent.parentElement;"
			+ "		}"
			+ "		ev.dataTransfer.setData(dataKey, sourceContent.id);"
			+ "	};"
			+ "	var swapResult = false;"
			+ "	tom.subSwap= function(data, url, method, synch) {"
			+ "		var httpRequest = tom.CreateRequestObj ();"
			+ "		swapResult = false;"
			+ "		try {"
			+ "			httpRequest.open (method, url, synch);"
			+ "			httpRequest.onreadystatechange = function () { tom.orscSwap (httpRequest); };"
			+ "			httpRequest.setRequestHeader(\"Content-Type\", \"application/x-www-form-urlencoded\");"
			+ "			httpRequest.send (data);"
			+ "		}"
			+ "		catch (e) {"
			+ "			alert (\"Cannot connect to the server!\");"
			+ "			return;"
			+ "		}"
			+ "	};"
			+ "	tom.orscSwap = function (httpRequest) {"
			+ "        if (httpRequest.readyState == 0 || httpRequest.readyState == 4) {"
			+ "            httpRequest.onreadystatechange = true;"
			+ "            if (tom.IsRequestSuccessful (httpRequest)) {"
			+ "            	var messageObj = httpRequest.responseXML.getElementsByTagName(\"update\")[0];"
			+ "            	if(messageObj)"
			+ "            	{"
			+ "            	    var message = messageObj.childNodes[0].nodeValue;"
			+ "            	    if (message == \"true\") {"
			+ "            	    	swapResult = true;"
			+ "            	    }"
			+ "            	}"
			+ "            }"
			+ "            else {"
			+ "                alert (\"An error occurred while registering. Please try it again.\");"
			+ "            }"
			+ "        }"
			+ "    };"
			+ "	tom.flipContentDrop = function (ev) {"
			+ "		ev.preventDefault();"
			+ "		var data = ev.dataTransfer.getData(dataKey);"
			+ "		var targetParent = ev.target;"
			+ "		while(!targetParent.id.contains(\"RowTD_\"))"
			+ "		{"
			+ "			targetParent = targetParent.parentElement;"
			+ "		}"
			+ "		var source = ev.dataTransfer.mozSourceNode.parentElement;"
			+ "		var targetChild = targetParent.childNodes[0];"
			+ "		var stuff=\"sourceCell=\"+source.id+\"&\"+\"displacee=\"+targetChild.id+\"&\"+\"targetCell=\"+targetParent.id+\"&\"+\"dropee=\"+data+\"&\"+\"javax.org.shutup.source=\"+source.id;"
			+ "		tom.subSwap(stuff, 'servlet/Swap', 'post', false, null);"
			+ "		tom.MultiAjax('servlet/Swap', 'post', stuff, null);"
			+ "		if(	!swapResult )"
			+ "		{"
			+ "			return;"
			+ "		}"
			+ "		targetParent.appendChild(document.getElementById(data));"
			+ "		source.appendChild(targetChild);"
			+ "	};"
			+ "	var tableElement;"
			+ "	var column =0;"
			+ "	var columnMax = 5;"
			+ "	var rowee = null;"
			+ "	var cars = 1;"
			+ "	tom.addOne = function (table)"
			+ "	{"
			+ "		tableElement = document.getElementById(table);"
			+ "		cars = cars + 1;"
			+ "		if(column > (columnMax-2) || rowee == null)"
			+ "		{"
			+ "			column = 0;"
			+ "			rowee=document.createElement(\"tr\");"
			+ "			var rowId = \"RowTR_\" + cars;"
			+ "			rowee.setAttribute(\"id\",rowId);"
			+ "			tableElement.appendChild(rowee);"
			+ "		}"
			+ "		else"
			+ "		{"
			+ "			column++;"
			+ "		}"
			+ "		var rowData=document.createElement(\"td\");"
			+ "		rowData.setAttribute(\"ondrop\",\"Tom.flipContentDrop(event)\");"
			+ "		rowData.setAttribute(\"ondragover\",\"Tom.allowDrop(event)\");"
			+ "		var rowId = \"RowTD_\" + cars;"
			+ "		rowData.setAttribute(\"id\",rowId);"
			+ "		var rowDiv=document.createElement(\"div\");"
			+ "		rowData.appendChild(rowDiv);"
			+ "		rowDiv.setAttribute(\"draggable\",\"true\");"
			+ "		rowDiv.setAttribute(\"ondragstart\",\"Tom.drag(event)\");"
			+ "		var divId = \"RowDiv_\" + cars;"
			+ "		rowDiv.setAttribute(\"id\",divId);"
			+ "		//"
			+ "		var identity=document.createElement(\"input\");"
			+ "		identity.setAttribute(\"hidden\",\"true\");"
			+ "		identity.setAttribute(\"value\",cars);"
			+ "		rowDiv.appendChild(identity);"
			+ "		//"
			+ "		var nodeImg=document.createElement(\"img\");"
			+ "		nodeImg.setAttribute(\"src\",\"Camry1987.jpg\");"
			+ "		nodeImg.setAttribute(\"height\",\"80\");"
			+ "		nodeImg.setAttribute(\"width\",\"130\");"
			+ "		nodeImg.setAttribute(\"draggable\",\"false\");"
			+ "		var id = \"CarImage_\" + cars;"
			+ "		nodeImg.setAttribute(\"id\",id);"
			+ "		rowDiv.appendChild(nodeImg);"
			+ "		rowDiv.appendChild(document.createElement(\"br\"));"
			+ "		//"
			+ "		var nodeName=document.createTextNode(\"Camry # \" + cars);"
			+ "		rowDiv.appendChild(nodeName);"
			+ "		rowDiv.appendChild(document.createElement(\"br\"));"
			+ "                //"
			+ "		var nodeDesc=document.createTextNode(\"My \" + cars + \" rat assed camry!\");"
			+ "		rowDiv.appendChild(nodeDesc);"
			+ "		//"
			+ "		rowee.appendChild(rowData);"
			+ "	};"
			+ "	var position = 100;"
			+ "	tom.forwardOne = function ()"
			+ "	{"
			+ "		var nodeDiv = selection;"
			+ "		if( nodeDiv == null)"
			+ "		{"
			+ "			nodeDiv = document.getElementById(\"movee\");"
			+ "		}"
			+ "		if(position < 399)"
			+ "		{"
			+ "			position = position + 100;"
			+ "		}"
			+ "		nodeDiv.style.top = position + \"px\";"
			+ "		nodeDiv.style.left = position + \"px\";"
			+ "	};"
			+ "	tom.backwardOne = function ()"
			+ "	{"
			+ "		var nodeDiv = selection;"
			+ "		if( nodeDiv == null)"
			+ "		{"
			+ "			nodeDiv = document.getElementById(\"movee\");"
			+ "		}"
			+ "		if(position > 99)"
			+ "		{"
			+ "			position = position - 100;"
			+ "		}"
			+ "		nodeDiv.style.top = position + \"px\";"
			+ "		nodeDiv.style.left = position + \"px\";"
			+ "	};"
			+ "	var direction = 1.1;"
			+ "	tom.clickOne = function (nodeDiv)"
			+ "	{"
			+ "		selection = nodeDiv;"
			+ "		var height = parseInt(nodeDiv.style.height) ;"
			+ "		if(height < 80)"
			+ "		{"
			+ "			direction = 1.1;"
			+ "		}"
			+ "		else if(height > 320)"
			+ "		{"
			+ "			direction = 0.9;"
			+ "		}"
			+ "		height *= direction;"
			+ "		var width  = parseInt(nodeDiv.style.width) * direction;"
			+ "		nodeDiv.style.height =  height +\"px\";"
			+ "		nodeDiv.style.width =  width + \"px\";"
			+ "		var child = nodeDiv.firstChild;"
			+ "		while(child){"
			+ "			if(child.nodeName.toLowerCase() == 'script'){"
			+ "				break;"
			+ "			}"
			+ "			child.setAttribute(\"height\",height);"
			+ "			child.setAttribute(\"width\",width);"
			+ "			child = child.nextSibling;"
			+ "		}"
			+ "	};"
			+ "	"
			+ "	"
			+ "	tom.CreateRequestObj = function () {"
			+ "		// although IE supports the XMLHttpRequest object, but it does not work on local files."
			+ "		var forceActiveX = (window.ActiveXObject && location.protocol === \"file:\");"
			+ "		if (window.XMLHttpRequest && !forceActiveX) {"
			+ "			return new XMLHttpRequest();"
			+ "		}"
			+ "		else {"
			+ "			try {"
			+ "				return new ActiveXObject(\"Microsoft.XMLHTTP\");"
			+ "			} catch(e) {}"
			+ "		}"
			+ "	};"
			+ ""
			+ "    // create HTTP request body form form data"
			+ "	tom.GetMessageBody = function (form) {"
			+ "		var data = \"\";"
			+ "		for (var i = 0; i < form.elements.length; i++) {"
			+ "			var elem = form.elements[i];"
			+ "			if (elem.name) {"
			+ "				var nodeName = elem.nodeName.toLowerCase ();"
			+ "				var type = elem.type ? elem.type.toLowerCase () : \"\";"
			+ ""
			+ "                // if an input:checked or input:radio is not checked, skip it"
			+ "				if (nodeName === \"input\" && (type === \"checkbox\" || type === \"radio\")) {"
			+ "					if (!elem.checked) {"
			+ "						continue;"
			+ "					}"
			+ "				}"
			+ "				var param = \"\";"
			+ "				// select element is special, if no value is specified the text must be sent"
			+ "				if (nodeName === \"select\") {"
			+ "					for (var j = 0; j < elem.options.length; j++) {"
			+ "						var option = elem.options[j];"
			+ "						if (option.selected) {"
			+ "							var valueAttr = option.getAttributeNode (\"value\");"
			+ "							var value = (valueAttr && valueAttr.specified) ? option.value : option.text;"
			+ "							if (param != \"\") {"
			+ "								param += \"&\";"
			+ "							}"
			+ "							param += encodeURIComponent (elem.name) + \"=\" + encodeURIComponent (value);"
			+ "						}"
			+ "					}"
			+ "				}"
			+ "				else {"
			+ "					param = encodeURIComponent (elem.name) + \"=\" + encodeURIComponent (elem.value);"
			+ "				}"
			+ "				if (data != \"\") {"
			+ "					data += \"&\";"
			+ "				}"
			+ "				data += param;                  "
			+ "			}"
			+ "		}"
			+ "		return data;"
			+ "	};"
			+ ""
			+ "	tom.IsRequestSuccessful = function (httpRequest) {"
			+ "        // IE: sometimes 1223 instead of 204"
			+ "    var success = (httpRequest.status == 0 || (httpRequest.status >= 200 && httpRequest.status < 300) || httpRequest.status == 304 || httpRequest.status == 1223);   "
			+ "    return success;"
			+ "	};	"
			+ "	"
			+ "	"
			+ "	tom.OnReadyStateChanged = function (httpRequest, form) {"
			+ "        if (httpRequest.readyState == 0 || httpRequest.readyState == 4) {"
			+ "            "
			+ "            registering = false;"
			+ "            tom.StopProgress();"
			+ "            "
			+ "                // prevent memory leaks"
			+ "            httpRequest.onreadystatechange = true;"
			+ ""
			+ "            if (tom.IsRequestSuccessful (httpRequest)) {    // defined in ajax_form.js"
			+ "            	var parser = new DOMParser();"
			+ "		        var xmlDoc = parser.parseFromString(httpRequest.responseText, \"application/xml\");"
			+ "            	var messageObj = xmlDoc.getElementsByTagName(\"update\")[0];"
			+ "            	var tableObj   = xmlDoc.getElementsByTagName(\"table\")[0];"
			+ "            	if(messageObj && tableObj)"
			+ "            	{"
			+ "                	var tableA = messageObj.innerHTML;"
			+ "                	var text = httpRequest.responseText;"
			+ "            	    var message = messageObj.childNodes[0].nodeValue;"
			+ "            	    if (message == \"true\") {"
			+ "            	    	var table = form.getElementsByTagName(\"table\")[0];"
			+ "            	    	var tableBody = table.getElementsByTagName(\"tbody\")[0];"
			+ "            	    	var x=tableObj.childNodes;"
			+ "            	    	for (var i=0;i<x.length;i++)"
			+ "            	    	  {"
			+ "                	    	var value = x[i];"
			+ "                	        var tr = document.createElement('tr');"
			+ "                	        var td = tr.appendChild(document.createElement('td'));"
			+ "                	        td.innerHTML = value.childNodes[0].innerHTML;"
			+ "                	        var td = tr.appendChild(document.createElement('td'));"
			+ "                	        td.innerHTML = value.childNodes[1].innerHTML;"
			+ "                	    	tableBody.appendChild(tr);"
			+ "            	    	  }"
			+ "            	    } else {"
			+ "            	    	msg.innerHTML = \"Subscription not valid\";"
			+ "            	    }            		"
			+ "            	}"
			+ "            	else if(messageObj)"
			+ "            	{"
			+ "            	    var message = messageObj.childNodes[0].nodeValue;"
			+ "            	    if (message == \"true\") {"
			+ "                        alert (\"Swap is good to go.\");"
			+ "            	    }"
			+ "            	}"
			+ "            }"
			+ "            else {"
			+ "                alert (\"An error occurred while registering. Please try it again.\");"
			+ "            }"
			+ "        }"
			+ "    };"
			+ "    tom.AjaxSend = function (data, url, method, synch) {"
			+ ""
			+ "            // send the request"
			+ "        var httpRequest = tom.CreateRequestObj ();"
			+ "            // try..catch is required if working offline"
			+ "        try {"
			+ "            httpRequest.open (method, url, synch);   // asynchronouse"
			+ "            httpRequest.onreadystatechange = function () {tom.OnReadyStateChanged (httpRequest, data); };"
			+ "            httpRequest.setRequestHeader(\"Content-Type\", \"application/x-www-form-urlencoded\");"
			+ "            httpRequest.send (data);"
			+ "        }"
			+ "        catch (e) {"
			+ "            alert (\"Cannot connect to the server!\");"
			+ "            return;"
			+ "        }"
			+ ""
			+ "        tom.StartProgress ();"
			+ "        return !httpRequest.onreadystatechange;"
			+ "    };"
			+ "    //////////////////////////// Multipart Ajax"
			+ "    tom.MultiAjax=function(){"
			+ "        try{"
			+ "          var xml       =new XMLHttpRequest();"
			+ "          var args      =arguments;"
			+ "          var context   =this;"
			+ "          var multipart =\"\";"
			+ "      "
			+ "          xml.open(args[0].mmethod,args[0].uurl,true);"
			+ "      "
			+ "          if(args[0].mmethod.search(/post/i)!=-1){"
			+ "            var boundary=Math.random().toString().substr(2);"
			+ "            xml.setRequestHeader(\"content-type\",\"multipart/form-data; charset=utf-8; boundary=\" + boundary);"
			+ "            for(var key in args[0].data){"
			+ "              multipart += \"--\" + boundary"
			+ "                         + \"\\r\\nContent-Disposition: form-data; name=\" + key"
			+ "                         + \"\\r\\nContent-type: application/octet-stream\""
			+ "                         + \"\\r\\n\\r\\n\" + args[0].data[key] + \"\\r\\n\";"
			+ "            }"
			+ "            multipart += \"--\"+boundary+\"--\\r\\n\";"
			+ "          }"
			+ "      "
			+ "          xml.onreadystatechange=function(){"
			+ "            try{"
			+ "              if(xml.readyState==4){"
			+ "                context.txt=xml.responseText;"
			+ "                context.xml=xml.responseXML;"
			+ "                args[0].callback();"
			+ "              }"
			+ "            }"
			+ "            catch(e){}"
			+ "          }"
			+ "      "
			+ "          xml.send(multipart);"
			+ "        }"
			+ "        catch(e){}"
			+ "    }"
			+ "    tom.ShowErrorFields = function()"
			+ "    {"
			+ "    	"
			+ "    };"
			+ "    tom.HideAllErrorFields = function()"
			+ "    {"
			+ "    	"
			+ "    };"
			+ "    tom.StartProgress = function()"
			+ "    {"
			+ "    	"
			+ "    };"
			+ "    tom.StopProgress = function()"
			+ "    {"
			+ "    	"
			+ "    };"
			+ "	"
			+ "})(window);"
			+ ""
			+ "";
}