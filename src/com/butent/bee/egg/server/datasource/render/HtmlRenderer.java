package com.butent.bee.egg.server.datasource.render;

import com.butent.bee.egg.server.datasource.base.ResponseStatus;
import com.butent.bee.egg.server.datasource.base.StatusType;
import com.butent.bee.egg.server.datasource.util.ValueFormatter;
import com.butent.bee.egg.shared.data.IsCell;
import com.butent.bee.egg.shared.data.IsColumn;
import com.butent.bee.egg.shared.data.IsRow;
import com.butent.bee.egg.shared.data.IsTable;
import com.butent.bee.egg.shared.data.Reasons;
import com.butent.bee.egg.shared.data.DataWarning;
import com.butent.bee.egg.shared.data.value.BooleanValue;
import com.butent.bee.egg.shared.data.value.ValueType;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import com.ibm.icu.util.ULocale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class HtmlRenderer {
  private static final Pattern BAD_JAVASCRIPT_REGEXP = Pattern.compile("javascript(( )*):");  

  private static final Pattern DETAILED_MESSAGE_A_TAG_REGEXP = Pattern.compile(
      "([^<]*<a(( )*target=\"_blank\")*(( )*target='_blank')*"
      + "(( )*href=\"[^\"]*\")*(( )*href='[^']*')*>[^<]*</a>)+[^<]*");

  private static final Logger logger = Logger.getLogger(HtmlRenderer.class.getName());

  public static CharSequence renderDataTable(IsTable dataTable, ULocale locale) {
    Document document = createDocument();
    Element bodyElement = appendHeadAndBody(document);

    Element tableElement = document.createElement("table");
    bodyElement.appendChild(tableElement);
    tableElement.setAttribute("border", "1");
    tableElement.setAttribute("cellpadding", "2");
    tableElement.setAttribute("cellspacing", "0");

    List<IsColumn> columns = dataTable.getColumns();
    Element trElement = document.createElement("tr");
    trElement.setAttribute("style", "font-weight: bold; background-color: #aaa;");
    for (IsColumn column : columns) {
      Element tdElement = document.createElement("td");
      tdElement.setTextContent(column.getLabel());
      trElement.appendChild(tdElement);
    }
    tableElement.appendChild(trElement);

    Map<ValueType, ValueFormatter> formatters = ValueFormatter.createDefaultFormatters(locale);
    int rowCount = 0;
    for (IsRow row : dataTable.getRows()) {
      rowCount++;
      trElement = document.createElement("tr");
      String backgroundColor = (rowCount % 2 != 0) ? "#f0f0f0" : "#ffffff";
      trElement.setAttribute("style", "background-color: " + backgroundColor);

      List<IsCell> cells = row.getCells();
      for (int c = 0; c < cells.size(); c++) {
        ValueType valueType = columns.get(c).getType();
        IsCell cell = cells.get(c);
        String cellFormattedText = cell.getFormattedValue();
        if (cellFormattedText == null) {
          cellFormattedText = formatters.get(cell.getType()).format(cell.getValue());
        }

        Element tdElement = document.createElement("td");
        if (cell.isNull()) {
          tdElement.setTextContent("\u00a0");
        } else {
          switch (valueType) {
            case NUMBER:
              tdElement.setAttribute("align", "right");
              tdElement.setTextContent(cellFormattedText);
              break;
            case BOOLEAN:
              BooleanValue booleanValue = (BooleanValue) cell.getValue();
              tdElement.setAttribute("align", "center");
              if (booleanValue.getValue()) {
                tdElement.setTextContent("\u2714");
              } else {
                tdElement.setTextContent("\u2717");
              }
              break;
            default:
              if (BeeUtils.isEmpty(cellFormattedText)) {
                tdElement.setTextContent("\u00a0");
              } else {
                tdElement.setTextContent(cellFormattedText);
              }
          }
        }
        trElement.appendChild(tdElement);
      }
      tableElement.appendChild(trElement);
    }
    bodyElement.appendChild(tableElement);

    for (DataWarning warning : dataTable.getWarnings()) {
      bodyElement.appendChild(document.createElement("br"));
      bodyElement.appendChild(document.createElement("br"));
      Element messageElement = document.createElement("div");
      messageElement.setTextContent(warning.getReasonType().getMessageForReasonType() + ". " +
          warning.getMessage());
      bodyElement.appendChild(messageElement);
    }

    return transformDocumentToHtmlString(document);
  }

  public static CharSequence renderHtmlError(ResponseStatus responseStatus) {
    StatusType status = responseStatus.getStatusType();
    Reasons reason = responseStatus.getReasonType();
    String detailedMessage = responseStatus.getDescription();

    Document document = createDocument();
    Element bodyElement = appendHeadAndBody(document);

    Element oopsElement = document.createElement("h3");
    oopsElement.setTextContent("Oops, an error occured.");
    bodyElement.appendChild(oopsElement);

    if (status != null) {
      String text = "Status: " + status.lowerCaseString();
      appendSimpleText(document, bodyElement, text);
    }

    if (reason != null) {
      String text = "Reason: " + reason.getMessageForReasonType();
      appendSimpleText(document, bodyElement, text);
    }

    if (detailedMessage != null) {
      String text = "Description: " + sanitizeDetailedMessage(detailedMessage);
      appendSimpleText(document, bodyElement, text);
    }

    return transformDocumentToHtmlString(document);
  }

  static String sanitizeDetailedMessage(String detailedMessage) {
    if (BeeUtils.isEmpty(detailedMessage)) {
      return "";
    }

    if (DETAILED_MESSAGE_A_TAG_REGEXP.matcher(detailedMessage).matches()
        && (!BAD_JAVASCRIPT_REGEXP.matcher(detailedMessage).find())) {
      return detailedMessage;
    } else {
      return EscapeUtil.htmlEscape(detailedMessage);
    }
  }

  private static Element appendHeadAndBody(Document document) {
    Element htmlElement = document.createElement("html");
    document.appendChild(htmlElement);
    Element headElement = document.createElement("head");
    htmlElement.appendChild(headElement);
    Element titleElement = document.createElement("title");
    titleElement.setTextContent("Google Visualization");
    headElement.appendChild(titleElement);
    Element bodyElement = document.createElement("body");
    htmlElement.appendChild(bodyElement);
    return bodyElement;
  }

  private static void appendSimpleText(Document document, Element bodyElement, String text) {
    Element statusElement = document.createElement("div");
    statusElement.setTextContent(text);
    bodyElement.appendChild(statusElement);
  }

  private static Document createDocument() {
    DocumentBuilder documentBuilder = null;
    try {
      documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LogUtils.severe(logger, e, "Couldn't create a document builder");
      throw new RuntimeException(
          "Couldn't create a document builder. This should never happen.", e);
    }
    Document document = documentBuilder.newDocument();
    return document;
  }

  private static String transformDocumentToHtmlString(Document document) {
    Transformer transformer = null;
    try {
      transformer = TransformerFactory.newInstance().newTransformer();
    } catch (TransformerConfigurationException e) {
      LogUtils.severe(logger, e, "Couldn't create a transformer");
      throw new RuntimeException("Couldn't create a transformer. This should never happen.", e);
    }
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD HTML 4.01//EN");
    transformer.setOutputProperty(OutputKeys.METHOD, "html");
    transformer.setOutputProperty(OutputKeys.VERSION, "4.01");
    
    DOMSource source = new DOMSource(document);
    Writer writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    try {
      transformer.transform(source, result);
    } catch (TransformerException e) {
      LogUtils.severe(logger, e, "Couldn't transform");
      throw new RuntimeException("Couldn't transform. This should never happen.", e);
    }

    return writer.toString();
  }

  private HtmlRenderer() {
  }
}
