package com.butent.bee.server.modules.transport;

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
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.Node;
import com.butent.bee.shared.html.builder.elements.Datalist;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.html.builder.elements.Form;
import com.butent.bee.shared.html.builder.elements.Input;
import com.butent.bee.shared.html.builder.elements.Input.Type;
import com.butent.bee.shared.html.builder.elements.Tbody;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.IOException;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
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

  private static String getQueryForm(LocalizableConstants constants) {
    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.no()));

    doc.getBody().text("not yet");

    return doc.build();
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
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doService(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doService(req, resp);
  }

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

      html = getQueryForm(constants);

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
        .onSubmit("return trCommandRegister()")
        .append(
            button().typeSubmit().addClass(STYLE_PREFIX + "Register").id(COMMAND_REGISTER_ID),
            input().type(Type.HIDDEN).id("register-language").name(HttpConst.PARAM_LOCALE));

    Form query = form().addClass(STYLE_PREFIX + "Command-Form-query")
        .name("query")
        .acceptCharsetUtf8()
        .methodPost()
        .action(req.getServletContext().getContextPath() + req.getServletPath() + PATH_QUERY)
        .onSubmit("return trCommandQuery()")
        .append(
            button().typeSubmit().addClass(STYLE_PREFIX + "Query").id(COMMAND_QUERY_ID),
            input().type(Type.HIDDEN).id("query-language").name(HttpConst.PARAM_LOCALE));

    return div().addClass(STYLE_PREFIX + "Command-container").append(register, query);
  }

  @Override
  protected String getLoginScriptName() {
    return "trlogin";
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
        regField(constants.trRegistrationAddress(), COL_REGISTRATION_ADDRESS, true),
        regField(constants.trRegistrationVatCode(), COL_REGISTRATION_VAT_CODE, false),
        regField(constants.trRegistrationCity(), COL_REGISTRATION_CITY, true),
        regField(constants.trRegistrationCountry(), COL_REGISTRATION_COUNTRY, true),
        regField(constants.trRegistrationContact(), COL_REGISTRATION_CONTACT, true),
        regField(constants.trRegistrationContactPosition(), COL_REGISTRATION_CONTACT_POSITION,
            true),
        regField(constants.trRegistrationEmail(), Type.EMAIL, COL_REGISTRATION_EMAIL, true),
        regField(constants.trRegistrationPhone(), Type.TEL, COL_REGISTRATION_PHONE, true),
        regField(constants.trRegistrationMobile(), Type.TEL, COL_REGISTRATION_MOBILE, false),
        regField(constants.trRegistrationFax(), COL_REGISTRATION_FAX, false),
        regField(constants.trRegistrationBank(), COL_REGISTRATION_BANK, false),
        regField(constants.trRegistrationBankAddress(), COL_REGISTRATION_BANK_ADDRESS, false),
        regField(constants.trRegistrationBankAccount(), COL_REGISTRATION_BANK_ACCOUNT, false),
        regField(constants.trRegistrationSwift(), COL_REGISTRATION_SWIFT, false),
        regField(constants.trRegistrationExchangeCode(), COL_REGISTRATION_EXCHANGE_CODE, false),
        regField(constants.trRegistrationNotes(), COL_REGISTRATION_NOTES, false));

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

    return doc.build(0, 2);
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

        switch (column.getType()) {
          case DATE_TIME:
            value = row.getDateTime(i).toCompactString();
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

    return doc.build(0, 0);
  }
}
