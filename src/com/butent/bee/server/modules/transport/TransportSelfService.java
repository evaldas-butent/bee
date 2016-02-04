package com.butent.bee.server.modules.transport;

import com.google.common.net.HttpHeaders;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.LoginServlet;
import com.butent.bee.server.ProxyBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.HttpConst;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.rest.RestResponse;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.ui.UiHolderBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.Node;
import com.butent.bee.shared.html.builder.elements.Form;
import com.butent.bee.shared.html.builder.elements.Input.Type;
import com.butent.bee.shared.html.builder.elements.Tbody;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

@WebServlet(urlPatterns = "/tr/*")
@SuppressWarnings("serial")
public class TransportSelfService extends LoginServlet {

  private static final String PATH_QUERY = "/query";

  private static BeeLogger logger = LogUtils.getLogger(TransportSelfService.class);

  @EJB
  ProxyBean proxy;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  UiHolderBean ui;
  @EJB
  ShipmentRequestsWorker worker;

  @Override
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    String html = null;
    String path = req.getPathInfo();

    if (BeeUtils.isEmpty(path)) {
      html = getInitialPage(req, UserInterface.SELF_SERVICE);

    } else if (BeeUtils.same(path, getQueryPath())) {
      Map<String, String> parameters = HttpUtils.getParameters(req, false);

      String language = getLanguage(req);
      Map<String, String> dictionary = Localizations.getPreferredDictionary(language);

      if (parameters.containsKey(COL_QUERY_LOADING_CITY)) {
        html = query(req, parameters);
      } else {
        html = getQuery(language, dictionary);
      }
    }
    if (BeeUtils.isEmpty(html)) {
      HttpUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, path);
      return;
    }
    HttpUtils.sendResponse(resp, html);
  }

  @Override
  protected Node getLoginExtension(HttpServletRequest req) {
    Form query = form().addClass(STYLE_PREFIX + "Command-Form-query")
        .name("query")
        .acceptCharsetUtf8()
        .methodPost()
        .action(req.getServletContext().getContextPath() + req.getServletPath() + PATH_QUERY)
        .onSubmit("setSelectedLanguage(this)")
        .append(
            button().typeSubmit().addClass(STYLE_PREFIX + "Query").id(COMMAND_QUERY_ID),
            input().type(Type.HIDDEN).id("query-language").name(HttpConst.PARAM_LOCALE));

    return div().addClass(STYLE_PREFIX + "Command-container").append(query);
  }

  @Override
  protected boolean isProtected(HttpServletRequest req) {
    return !BeeUtils.same(req.getPathInfo(), PATH_QUERY) && super.isProtected(req);
  }

  protected static String getQueryPath() {
    return PATH_QUERY;
  }

  private String query(HttpServletRequest req, Map<String, String> parameters) {
    Map<Integer, Map<String, String>> handling = new TreeMap<>();
    JsonObjectBuilder json = Json.createObjectBuilder();

    json.add(COL_QUERY_HOST, req.getRemoteAddr());
    json.add(COL_QUERY_AGENT, req.getHeader(HttpHeaders.USER_AGENT));

    for (String key : parameters.keySet()) {
      String value = parameters.get(key);

      if (!BeeUtils.isEmpty(value)) {
        if (BeeUtils.startsWith(key, VAR_LOADING) || BeeUtils.startsWith(key, VAR_UNLOADING)) {
          String[] arr = key.split("-", 2);
          key = arr[0];
          Integer idx = BeeUtils.toInt(ArrayUtils.getQuietly(arr, 1));

          if (!handling.containsKey(idx)) {
            handling.put(idx, new HashMap<>());
          }
          handling.get(idx).put(key, value);
        } else {
          json.add(key, value);
        }
      }
    }
    JsonArrayBuilder array = Json.createArrayBuilder();

    for (Map<String, String> map : handling.values()) {
      JsonObjectBuilder place = Json.createObjectBuilder();

      for (Map.Entry<String, String> entry : map.entrySet()) {
        place.add(entry.getKey(), entry.getValue());
      }
      array.add(place);
    }
    RestResponse result = worker.request(json.add(TBL_CARGO_HANDLING, array).build());

    return (result.hasError() ? result.getStatus() : result.getResult()).toString();
  }

  private String doQuery(HttpServletRequest req, Map<String, String> parameters,
      LocalizableConstants constants, Map<String, String> dictionary) {

    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.trRequestNew()));

    List<BeeColumn> columns = sys.getView(VIEW_SHIPMENT_REQUESTS).getRowSetColumns();
    BeeRow row = DataUtils.createEmptyRow(columns.size());

    for (int i = 0; i < columns.size(); i++) {
      BeeColumn column = columns.get(i);
      String colId = column.getId();
      String value = null;

      switch (colId) {
        case COL_QUERY_DATE:
          row.setValue(i, TimeUtils.nowMinutes());
          break;

        case COL_QUERY_STATUS:
          row.setValue(i, CargoRequestStatus.NEW.ordinal());
          break;

        case ALS_CARGO_DESCRIPTION:
          value = BeeUtils.notEmpty(parameters.get(colId), DEFAULT_CARGO_DESCRIPTION);
          break;

        case COL_QUERY_HOST:
          value = req.getRemoteAddr();
          break;

        case COL_QUERY_AGENT:
          value = req.getHeader(HttpHeaders.USER_AGENT);
          break;
        case COL_USER_INTERFACE:
          row.setValue(i, UserInterface.normalize(UserInterface.SELF_SERVICE).ordinal());
          break;

        default:
          if (parameters.containsKey(colId)) {
            value = parameters.get(colId);
          }
      }

      if (!BeeUtils.isEmpty(value)) {
        switch (column.getType()) {
          case BOOLEAN:
            row.setValue(i, true);
            break;

          case DATE:
            row.setValue(i, TimeUtils.parseDate(value));
            break;

          case DATE_TIME:
            row.setValue(i, TimeUtils.parseDateTime(value));
            break;

          case DECIMAL:
            row.setValue(i, BeeUtils.toDecimalOrNull(value));
            break;

          case INTEGER:
            row.setValue(i, BeeUtils.toIntOrNull(value));
            break;

          case LONG:
            row.setValue(i, BeeUtils.toLongOrNull(value));
            break;

          case NUMBER:
            row.setValue(i, BeeUtils.toDoubleOrNull(value));
            break;

          default:
            row.setValue(i, value.trim());
        }
      }
    }

    BeeRowSet insert = DataUtils.createRowSetForInsert(VIEW_SHIPMENT_REQUESTS, columns, row);
    ResponseObject response = proxy.commitRow(insert);

    if (response.hasErrors()) {
      for (String message : response.getErrors()) {
        doc.getBody().append(div().text(message));
      }

    } else if (response.hasResponse(BeeRow.class)) {
      row = (BeeRow) response.getResponse();

      Tbody fields = tbody();

      for (int i = 0; i < columns.size(); i++) {
        if (row.isNull(i)) {
          continue;
        }

        BeeColumn column = columns.get(i);
        String value;

        switch (column.getId()) {
          case COL_QUERY_STATUS:
          case COL_QUERY_HOST:
          case COL_QUERY_AGENT:
          case COL_USER_INTERFACE:
            value = null;
            break;

          default:
            value = (column.getType() == ValueType.LONG) ? null : DataUtils.render(column, row, i);
        }

        if (!BeeUtils.isEmpty(value)) {
          String label = Localized.maybeTranslate(column.getLabel(), dictionary);

          fields.append(tr().append(
              td().alignRight().paddingRight(1, CssUnit.EM).text(label),
              td().text(value)));
        }
      }

      doc.getBody().append(h3().text(constants.trRequestReceived()), table().append(fields));

    } else if (response.hasMessages()) {
      for (ResponseMessage message : response.getMessages()) {
        doc.getBody().append(div().text(message.getMessage()));
      }

    } else {
      doc.getBody().append(div().text(BeeUtils.joinWords("response", response.getType(),
          response.getResponse())));
    }

    return doc.buildLines();
  }

  private String getQuery(String locale, Map<String, String> dictionary) {
    org.w3c.dom.Document form = ui.getFormDocument("ShipmentRequestQuery", false);

    for (String tag : new String[] {"label", "legend", "title", "button"}) {
      NodeList captions = form.getElementsByTagName(tag);

      for (int i = 0; i < captions.getLength(); i++) {
        org.w3c.dom.Node node = captions.item(i);
        node.setTextContent(Localized.maybeTranslate(node.getTextContent(), dictionary));
      }
    }
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();

    try {
      String attr = "placeholder";
      NodeList placeholders = (NodeList) xpath.evaluate("//*[@" + attr + "]", form,
          XPathConstants.NODESET);

      for (int i = 0; i < placeholders.getLength(); i++) {
        org.w3c.dom.Element node = (org.w3c.dom.Element) placeholders.item(i);
        node.setAttribute(attr, Localized.maybeTranslate(node.getAttribute(attr), dictionary));
      }
    } catch (XPathExpressionException e) {
      logger.error(e);
    }
    for (String tag : new String[] {"select", "datalist"}) {
      NodeList lists = form.getElementsByTagName(tag);

      for (int i = 0; i < lists.getLength(); i++) {
        org.w3c.dom.Element node = (org.w3c.dom.Element) lists.item(i);
        String[] id = BeeUtils.split(node.getAttribute("id"), '-');

        if (ArrayUtils.length(id) == 2) {
          String tbl = id[0];
          String fld = id[1];

          if (sys.isTable(tbl)) {
            SqlSelect query = new SqlSelect()
                .addFields(tbl, fld)
                .addFrom(tbl);

            if (sys.hasField(tbl, COL_SELF_SERVICE)) {
              query.setWhere(SqlUtils.notNull(tbl, COL_SELF_SERVICE))
                  .addOrder(tbl, COL_SELF_SERVICE);
            }
            for (String value : qs.getColumn(query.addOrder(tbl, fld))) {
              org.w3c.dom.Element opt = form.createElement("option");
              opt.setTextContent(value);
              node.appendChild(opt);
            }
          }
        }
      }
    }
    NodeList lists = form.getElementsByTagName("form");

    for (int i = 0; i < lists.getLength(); i++) {
      Element loc = form.createElement("input");
      loc.setAttribute("type", "hidden");
      loc.setAttribute("name", COL_USER_LOCALE);
      loc.setAttribute("value", locale);
      lists.item(i).appendChild(loc);
    }
    return XmlUtils.toString(form, false);
  }
}
