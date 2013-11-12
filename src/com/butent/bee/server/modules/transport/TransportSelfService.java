package com.butent.bee.server.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.LoginServlet;
import com.butent.bee.server.ProxyBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.HttpConst;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.Node;
import com.butent.bee.shared.html.builder.elements.Datalist;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.html.builder.elements.Fieldset;
import com.butent.bee.shared.html.builder.elements.Form;
import com.butent.bee.shared.html.builder.elements.Input;
import com.butent.bee.shared.html.builder.elements.Select;
import com.butent.bee.shared.html.builder.elements.Span;
import com.butent.bee.shared.html.builder.elements.Input.Type;
import com.butent.bee.shared.html.builder.elements.Tbody;
import com.butent.bee.shared.html.builder.elements.Textarea;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/tr/*")
@SuppressWarnings("serial")
public class TransportSelfService extends LoginServlet {

  private static final String PATH_REGISTER = "/register";
  private static final String PATH_QUERY = "/query";

  private static final String REG_STYLE_PREFIX = "bee-tr-registration-";

  private static final String REG_STYLE_LABEL_CELL = REG_STYLE_PREFIX + "label-cell";
  private static final String REG_STYLE_LABEL = REG_STYLE_PREFIX + "label";

  private static final String REG_STYLE_INPUT_CELL = REG_STYLE_PREFIX + "input-cell";
  private static final String REG_STYLE_INPUT = REG_STYLE_PREFIX + "input";

  private static final String REG_STYLE_REQUIRED = REG_STYLE_PREFIX + "required";

  private static final String Q_STYLE_PREFIX = "bee-tr-query-";

  private static final String Q_STYLE_LABEL_CELL = Q_STYLE_PREFIX + "label-cell";
  private static final String Q_STYLE_LABEL = Q_STYLE_PREFIX + "label";

  private static final String Q_STYLE_INPUT_CELL = Q_STYLE_PREFIX + "input-cell";
  private static final String Q_STYLE_INPUT = Q_STYLE_PREFIX + "input";

  private static final String Q_STYLE_REQUIRED = Q_STYLE_PREFIX + "required";

  private static final String Q_STYLE_GROUP = Q_STYLE_PREFIX + "group";
  private static final String Q_STYLE_LEGEND = Q_STYLE_PREFIX + "legend";
  private static final String Q_STYLE_TABLE = Q_STYLE_PREFIX + "table";

  private static final String Q_STYLE_CHECK = Q_STYLE_PREFIX + "check";
  private static final String Q_STYLE_AREA = Q_STYLE_PREFIX + "area";

  private static Element qArea(String label, String name) {
    Textarea input = textarea().addClass(Q_STYLE_AREA).id(name + ID_SUFFIX_INPUT).name(name);

    return tr().id(name + ID_SUFFIX_FIELD).append(
        queryLabelCell(name + ID_SUFFIX_LABEL, label, false),
        td().addClass(Q_STYLE_INPUT_CELL).append(input));
  }

  private static Node qCheck(String label, String name) {
    Input input = input().type(Type.CHECK_BOX).name(name).id(name + ID_SUFFIX_INPUT);
    Span span = span().id(name + ID_SUFFIX_LABEL).text(label);

    return label().addClass(Q_STYLE_CHECK).id(name + ID_SUFFIX_FIELD).append(input, span);
  }

  private static Element qField(String label, String name, boolean required) {
    return qField(label, Type.TEXT, name, required);
  }

  private static Element qField(String label, Type type, String name, boolean required) {
    return tr().id(name + ID_SUFFIX_FIELD).append(
        queryLabelCell(name + ID_SUFFIX_LABEL, label, required),
        queryInputCell(name + ID_SUFFIX_INPUT, type, name, required));
  }

  private static Element qGroup(String caption, Tbody fields) {
    return qGroup(caption, fields, null);
  }

  private static Element qGroup(String caption, Tbody fields, Node node) {
    Fieldset group = fieldset().addClass(Q_STYLE_GROUP).append(
        legend().addClass(Q_STYLE_LEGEND).text(caption),
        table().addClass(Q_STYLE_TABLE).append(fields));

    if (node != null) {
      group.appendChild(node);
    }
    return group;
  }

  private static Node queryInputCell(String id, Type type, String name, boolean required) {
    Input input = input().addClass(Q_STYLE_INPUT).id(id).type(type).name(name);
    if (required) {
      input.required();
    }

    return td().addClass(Q_STYLE_INPUT_CELL).append(input);
  }

  private static Node queryLabelCell(String id, String text, boolean required) {
    Div div = div().addClass(Q_STYLE_LABEL);
    if (required) {
      div.addClass(Q_STYLE_REQUIRED);
    }
    div.id(id).text(text);

    return td().addClass(Q_STYLE_LABEL_CELL).append(div);
  }

  private static Element regField(String label, String name, boolean required) {
    return regField(label, Type.TEXT, name, required);
  }

  private static Element regField(String label, Type type, String name, boolean required) {
    return tr().id(name + ID_SUFFIX_FIELD).append(
        registrationLabelCell(name + ID_SUFFIX_LABEL, label, required),
        registrationInputCell(name + ID_SUFFIX_INPUT, type, name, required));
  }

  private static Node registrationInputCell(String id, Type type, String name, boolean required) {
    Input input = input().addClass(REG_STYLE_INPUT).id(id).type(type).name(name);
    if (required) {
      input.required();
    }

    return td().addClass(REG_STYLE_INPUT_CELL).append(input);
  }

  private static Node registrationLabelCell(String id, String text, boolean required) {
    Div div = div().addClass(REG_STYLE_LABEL);
    if (required) {
      div.addClass(REG_STYLE_REQUIRED);
    }
    div.id(id).text(text);

    return td().addClass(REG_STYLE_LABEL_CELL).append(div);
  }

  @EJB
  ProxyBean proxy;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  @Override
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    String html;
    String path = req.getPathInfo();

    if (BeeUtils.isEmpty(path)) {
      html = getInitialPage(req, UserInterface.SELF_SERVICE);

    } else if (BeeUtils.same(path, PATH_REGISTER)) {
      Map<String, String> parameters = HttpUtils.getParameters(req, false);

      String language = getLanguage(req);
      LocalizableConstants constants = Localizations.getPreferredConstants(language);

      if (parameters.containsKey(COL_REGISTRATION_COMPANY_NAME)) {
        Map<String, String> dictionary = Localizations.getPreferredDictionary(language);
        html = register(req, parameters, constants, dictionary);
      } else {
        html = getRegistrationForm(req.getServletContext().getContextPath(), constants);
      }

    } else if (BeeUtils.same(path, PATH_QUERY)) {
      String language = getLanguage(req);
      LocalizableConstants constants = Localizations.getPreferredConstants(language);

      html = getQueryForm(req.getServletContext().getContextPath(), constants);

    } else {
      HttpUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, path);
      return;
    }

    HttpUtils.sendResponse(resp, html);
  }
  
  @Override
  protected Node getLoginExtension(HttpServletRequest req) {
    Form register = form().addClass(STYLE_PREFIX + "Command-Form-register")
        .name("register")
        .acceptCharsetUtf8()
        .methodPost()
        .action(req.getServletContext().getContextPath() + req.getServletPath() + PATH_REGISTER)
        .onSubmit("setSelectedLanguage(this)")
        .append(
            button().typeSubmit().addClass(STYLE_PREFIX + "Register").id(COMMAND_REGISTER_ID),
            input().type(Type.HIDDEN).id("register-language").name(HttpConst.PARAM_LOCALE));

    Form query = form().addClass(STYLE_PREFIX + "Command-Form-query")
        .name("query")
        .acceptCharsetUtf8()
        .methodPost()
        .action(req.getServletContext().getContextPath() + req.getServletPath() + PATH_QUERY)
        .onSubmit("setSelectedLanguage(this)")
        .append(
            button().typeSubmit().addClass(STYLE_PREFIX + "Query").id(COMMAND_QUERY_ID),
            input().type(Type.HIDDEN).id("query-language").name(HttpConst.PARAM_LOCALE));

    return div().addClass(STYLE_PREFIX + "Command-container").append(register, query);
  }

  private String getQueryForm(String contextPath, LocalizableConstants constants) {
    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.trRequestNew()),
        link().styleSheet(resource(contextPath, Paths.getStyleSheetPath("trquery"))));

    Div fieldPanel = div().addClass(Q_STYLE_PREFIX + "fieldPanel");

    Tbody customerFields = tbody().append(
        qField(constants.trRequestCustomerName(), COL_QUERY_CUSTOMER_NAME, true),
        qField(constants.trRequestCustomerCode(), COL_QUERY_CUSTOMER_CODE, true),
        qField(constants.trRequestCustomerVatCode(), COL_QUERY_CUSTOMER_CODE, false),
        qField(constants.trRequestCustomerContact(), COL_QUERY_CUSTOMER_CONTACT, true),
        qField(constants.trRequestCustomerContactPosition(), COL_QUERY_CUSTOMER_CONTACT_POSITION,
            true),
        qField(constants.trRequestCustomerAddress(), COL_QUERY_CUSTOMER_ADDRESS, true),
        qField(constants.trRequestCustomerPhone(), Type.TEL, COL_QUERY_CUSTOMER_PHONE, true),
        qField(constants.trRequestCustomerEmail(), Type.EMAIL, COL_QUERY_CUSTOMER_EMAIL, true),
        qField(constants.trRequestCustomerExchangeCode(), COL_QUERY_CUSTOMER_EXCHANGE_CODE, false));

    fieldPanel.append(qGroup(constants.trRequestCustomerInfo(), customerFields));

    Tbody loadingFields =
        tbody().append(
            qField(constants.trRequestPlaceCompanyName(), COL_QUERY_LOADING_COMPANY_NAME, true),
            qField(constants.trRequestPlaceContact(), loadingColumnAlias(COL_PLACE_CONTACT), true),
            qField(constants.address(), loadingColumnAlias(COL_PLACE_ADDRESS), true),
            qField(constants.city(), COL_QUERY_LOADING_CITY, true),
            qField(constants.country(), loadingColumnAlias(COL_PLACE_COUNTRY), false),
            qField(constants.phone(), Type.TEL, loadingColumnAlias(COL_PLACE_PHONE), true),
            qField(constants.email(), Type.EMAIL, COL_QUERY_LOADING_EMAIL, false),
            qField(constants.trRequestPlaceFax(), loadingColumnAlias(COL_PLACE_FAX), false),
            qField(constants.trLoadingDate(), Type.DATE_TIME, loadingColumnAlias(COL_PLACE_DATE),
                true),
            qField(constants.trLoadingNumber(), loadingColumnAlias(COL_PLACE_NUMBER), false));

    fieldPanel.append(qGroup(constants.trLoadingInfo(), loadingFields));

    Tbody unloadingFields = tbody().append(
        qField(constants.trRequestPlaceCompanyName(), COL_QUERY_UNLOADING_COMPANY_NAME, true),
        qField(constants.trRequestPlaceContact(), unloadingColumnAlias(COL_PLACE_CONTACT), true),
        qField(constants.address(), unloadingColumnAlias(COL_PLACE_ADDRESS), true),
        qField(constants.city(), COL_QUERY_UNLOADING_CITY, true),
        qField(constants.country(), unloadingColumnAlias(COL_PLACE_COUNTRY), false),
        qField(constants.phone(), Type.TEL, unloadingColumnAlias(COL_PLACE_PHONE), true),
        qField(constants.email(), Type.EMAIL, COL_QUERY_UNLOADING_EMAIL, false),
        qField(constants.trRequestPlaceFax(), unloadingColumnAlias(COL_PLACE_FAX), false),
        qField(constants.trUnloadingDate(), Type.DATE_TIME, unloadingColumnAlias(COL_PLACE_DATE),
            true),
        qField(constants.trUnloadingNumber(), unloadingColumnAlias(COL_PLACE_NUMBER), false));

    fieldPanel.append(qGroup(constants.trUnloadingInfo(), unloadingFields));
    
    Tbody shipmentFields = tbody().append(
        qSelector(constants.trRequestExpeditionType(), COL_QUERY_EXPEDITION,
            VIEW_EXPEDITION_TYPES, ComparisonFilter.notEmpty(COL_EXPEDITION_TYPE_SELF_SERVICE),
            Order.ascending(COL_EXPEDITION_TYPE_SELF_SERVICE, COL_EXPEDITION_TYPE_NAME),
            Lists.newArrayList(COL_EXPEDITION_TYPE_NAME), true),
        qSelector(constants.trRequestShippingTerms(), COL_CARGO_SHIPPING_TERM,
            VIEW_SHIPPING_TERMS, ComparisonFilter.notEmpty(COL_SHIPPING_TERM_SELF_SERVICE),
            Order.ascending(COL_SHIPPING_TERM_SELF_SERVICE, COL_SHIPPING_TERM_NAME),
            Lists.newArrayList(COL_SHIPPING_TERM_NAME), true),
        qField(constants.trRequestDeliveryDate(), Type.DATE, COL_QUERY_DELIVERY_DATE, false),
        qField(constants.trRequestDeliveryTime(), Type.TIME, COL_QUERY_DELIVERY_TIME, false),
        qField(constants.trRequestTermsOfDelivery(), COL_QUERY_TERMS_OF_DELIVERY, false));

    fieldPanel.append(qGroup(constants.trRequestShipmentInfo(), shipmentFields,
        div().append(
            qCheck(constants.trRequestCustomsBrokerage(), COL_QUERY_CUSTOMS_BROKERAGE),
            qCheck(constants.trRequestFreightInsurance(), COL_QUERY_FREIGHT_INSURANCE))));

    Tbody cargoFields = tbody().append(
        qField(constants.trRequestCargoDescription(), COL_CARGO_DESCRIPTION, false),
        qField(constants.trRequestCargoQuantity(), Type.NUMBER, COL_CARGO_QUANTITY, false),
        qField(constants.trRequestCargoWeight(), COL_CARGO_WEIGHT, true),
        qField(constants.trRequestCargoVolume(), COL_CARGO_VOLUME, false),
        qField(constants.trRequestCargoLdm(), COL_CARGO_LDM, false),
        qField(constants.trRequestCargoLength(), COL_CARGO_LENGTH, false),
        qField(constants.trRequestCargoWidth(), COL_CARGO_WIDTH, false),
        qField(constants.trRequestCargoHeight(), COL_CARGO_HEIGHT, false),
        qField(constants.trRequestCargoPalettes(), COL_CARGO_PALETTES, false),
        qField(constants.trRequestCargoValue(), COL_CARGO_VALUE, false),
        qSelector(constants.trRequestCargoCurrency(), COL_CARGO_VALUE_CURRENCY,
            CommonsConstants.VIEW_CURRENCIES, CommonsConstants.COL_CURRENCY_NAME, false));

    fieldPanel.append(qGroup(constants.trRequestCargoInfo(), cargoFields));

    Tbody additionalFields = tbody().append(
        qSelector(constants.trRequestResponsibleManager(), COL_QUERY_MANAGER,
            CommonsConstants.VIEW_USERS, null,
            Order.ascending(CommonsConstants.COL_LAST_NAME, CommonsConstants.COL_FIRST_NAME),
            Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME),
            false),
        qArea(constants.trRequestNotes(), COL_QUERY_NOTES));

    fieldPanel.append(qGroup(constants.trRequestAdditionalInfo(), additionalFields));
    
    doc.getBody().append(
        div().addClass(Q_STYLE_PREFIX + "panel").append(
            div().addClass(Q_STYLE_PREFIX + "caption").text(constants.trRequestNew()),
            form().addClass(Q_STYLE_PREFIX + "form").acceptCharsetUtf8().methodPost().append(
                fieldPanel,
                input().type(Type.HIDDEN).name(HttpConst.PARAM_LOCALE)
                    .value(constants.languageTag()),
                button().typeSubmit().addClass(Q_STYLE_PREFIX + "submit")
                    .text(constants.trRequestActionSubmit()))));

    return doc.buildLines();
  }

  private String getRegistrationForm(String contextPath, LocalizableConstants constants) {
    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.trRegistrationNew()),
        link().styleSheet(resource(contextPath, Paths.getStyleSheetPath("trregistration"))));

    Tbody fields = tbody().append(
        regField(constants.trRegistrationCompanyName(), COL_REGISTRATION_COMPANY_NAME, true),
        regField(constants.trRegistrationCompanyCode(), COL_REGISTRATION_COMPANY_CODE, true),
        regField(constants.trRegistrationVatCode(), COL_REGISTRATION_VAT_CODE, false),
        regField(constants.trRegistrationContact(), COL_REGISTRATION_CONTACT, true),
        regField(constants.trRegistrationContactPosition(), COL_REGISTRATION_CONTACT_POSITION,
            true),
        regField(constants.trRegistrationAddress(), COL_REGISTRATION_ADDRESS, true),
        regField(constants.trRegistrationCity(), COL_REGISTRATION_CITY, true),
        regField(constants.trRegistrationCountry(), COL_REGISTRATION_COUNTRY, true),
        regField(constants.trRegistrationEmail(), Type.EMAIL, COL_REGISTRATION_EMAIL, true),
        regField(constants.trRegistrationPhone(), Type.TEL, COL_REGISTRATION_PHONE, true),
        regField(constants.trRegistrationMobile(), Type.TEL, COL_REGISTRATION_MOBILE, false),
        regField(constants.trRegistrationFax(), COL_REGISTRATION_FAX, false),
        regField(constants.trRegistrationBank(), COL_REGISTRATION_BANK, false),
        regField(constants.trRegistrationBankAddress(), COL_REGISTRATION_BANK_ADDRESS, false),
        regField(constants.trRegistrationBankAccount(), COL_REGISTRATION_BANK_ACCOUNT, false),
        regField(constants.trRegistrationSwift(), COL_REGISTRATION_SWIFT, false),
        regField(constants.trRegistrationExchangeCode(), COL_REGISTRATION_EXCHANGE_CODE, false));

    doc.getBody().append(
        div().addClass(REG_STYLE_PREFIX + "panel").append(
            div().addClass(REG_STYLE_PREFIX + "caption")
                .text(constants.trRegistrationFormCaption()),
            form().addClass(REG_STYLE_PREFIX + "form").acceptCharsetUtf8().methodPost().append(
                table().addClass(REG_STYLE_PREFIX + "table").append(fields),
                input().type(Type.HIDDEN).name(HttpConst.PARAM_LOCALE)
                    .value(constants.languageTag()),
                button().typeSubmit().addClass(REG_STYLE_PREFIX + "submit")
                    .text(constants.trRegistrationActionSubmit()))));

    Datalist cities = proxy.getDataList(CommonsConstants.TBL_CITIES,
        CommonsConstants.COL_CITY_NAME);
    if (cities != null) {
      String listId = COL_REGISTRATION_CITY + ID_SUFFIX_LIST;
      cities.id(listId);

      Element element = fields.queryId(COL_REGISTRATION_CITY + ID_SUFFIX_INPUT);
      if (element instanceof Input) {
        ((Input) element).list(listId);
        doc.getBody().append(cities);
      }
    }

    Datalist countries = proxy.getDataList(CommonsConstants.TBL_COUNTRIES,
        CommonsConstants.COL_COUNTRY_NAME);
    if (countries != null) {
      String listId = COL_REGISTRATION_COUNTRY + ID_SUFFIX_LIST;
      countries.id(listId);

      Element element = fields.queryId(COL_REGISTRATION_COUNTRY + ID_SUFFIX_INPUT);
      if (element instanceof Input) {
        ((Input) element).list(listId);
        doc.getBody().append(countries);
      }
    }

    return doc.buildLines();
  }

  private Node qSelector(String label, String name, String viewName, Filter filter, Order order,
      List<String> columns, boolean required) {

    Select select = select().name(name);
    if (required) {
      select.required();
    } else {
      select.append(option());
    }

    BeeRowSet data = qs.getViewData(viewName, filter, order, columns);
    if (!DataUtils.isEmpty(data)) {
      for (BeeRow row : data.getRows()) {
        List<String> values = new ArrayList<>();

        for (String colName : columns) {
          String value = DataUtils.getString(data, row, colName);
          if (!BeeUtils.isEmpty(value)) {
            values.add(value);
          }
        }
        
        if (!values.isEmpty()) {
          select.append(option().value(row.getId())
              .text(BeeUtils.join(BeeConst.STRING_SPACE, values)));
        }
      }
    }

    return tr().id(name + ID_SUFFIX_FIELD).append(
        queryLabelCell(name + ID_SUFFIX_LABEL, label, required),
        td().addClass(Q_STYLE_INPUT_CELL).append(select));
  }

  private Node qSelector(String label, String name, String viewName, String colName,
      boolean required) {
    return qSelector(label, name, viewName, ComparisonFilter.notEmpty(colName),
        new Order(colName, true), Lists.newArrayList(colName), required);
  }

  private String register(HttpServletRequest req, Map<String, String> parameters,
      LocalizableConstants constants, Map<String, String> dictionary) {

    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.trRegistrationNew()));

    SqlInsert si = new SqlInsert(TBL_REGISTRATIONS);

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      if (!BeeUtils.isEmpty(entry.getValue()) && sys.hasField(TBL_REGISTRATIONS, entry.getKey())) {
        si.addConstant(entry.getKey(), entry.getValue());
      }
    }

    si.addConstant(COL_REGISTRATION_DATE, TimeUtils.nowMinutes());
    si.addConstant(COL_REGISTRATION_HOST, req.getRemoteAddr());
    si.addConstant(COL_REGISTRATION_AGENT, req.getHeader(HttpHeaders.USER_AGENT));

    ResponseObject response = proxy.insert(si);
    if (response.hasErrors()) {
      for (String message : response.getErrors()) {
        doc.getBody().append(div().text(message));
      }

    } else if (response.hasResponse(Long.class)) {
      BeeRowSet data = qs.getViewData(VIEW_REGISTRATIONS,
          IdFilter.compareId((Long) response.getResponse()));
      BeeRow row = data.getRow(0);

      Tbody fields = tbody();

      for (int i = 0; i < data.getNumberOfColumns(); i++) {
        BeeColumn column = data.getColumn(i);
        String value;

        switch (column.getId()) {
          case COL_REGISTRATION_DATE:
            value = row.getDateTime(i).toCompactString();
            break;

          case COL_REGISTRATION_HOST:
          case COL_REGISTRATION_AGENT:
            value = null;
            break;

          default:
            value = row.getString(i);
        }

        if (!BeeUtils.isEmpty(value)) {
          String label = Localized.maybeTranslate(column.getLabel(), dictionary);

          fields.append(tr().append(
              td().alignRight().paddingRight(1, CssUnit.EM).text(label),
              td().text(value)));
        }
      }

      doc.getBody().append(h3().text(constants.trRegistrationReceived()), table().append(fields));

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
}
