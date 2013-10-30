package com.butent.bee.server.modules.ec;

import com.google.common.net.HttpHeaders;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.server.LoginServlet;
import com.butent.bee.server.ProxyBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.Node;
import com.butent.bee.shared.html.builder.elements.Datalist;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.html.builder.elements.Input;
import com.butent.bee.shared.html.builder.elements.Input.Type;
import com.butent.bee.shared.html.builder.elements.Select;
import com.butent.bee.shared.html.builder.elements.Span;
import com.butent.bee.shared.html.builder.elements.Tbody;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.ec.EcConstants.EcClientType;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/ec/*")
@SuppressWarnings("serial")
public class EcServlet extends LoginServlet {

  private static final String PATH_REGISTER = "/register";

  private static final String REG_STYLE_PREFIX = "bee-ec-registration-";

  private static final String REG_STYLE_LABEL_CELL = REG_STYLE_PREFIX + "label-cell";
  private static final String REG_STYLE_LABEL = REG_STYLE_PREFIX + "label";

  private static final String REG_STYLE_INPUT_CELL = REG_STYLE_PREFIX + "input-cell";
  private static final String REG_STYLE_INPUT = REG_STYLE_PREFIX + "input";

  private static final String REG_STYLE_REQUIRED = REG_STYLE_PREFIX + "required";

  private static final String REG_STYLE_TYPE_PREFIX = "bee-ec-registration-type-";

  private static final String ID_SUFFIX_FIELD = "-field";
  private static final String ID_SUFFIX_LABEL = "-label";
  private static final String ID_SUFFIX_INPUT = "-input";
  private static final String ID_SUFFIX_LIST = "-list";

  private static Node clientTypeSelector(LocalizableConstants constants) {
    String name = COL_REGISTRATION_TYPE;

    Div container = div().addClass(REG_STYLE_TYPE_PREFIX + "container");

    for (EcClientType clientType : EcClientType.values()) {
      Input input = input().addClass(REG_STYLE_TYPE_PREFIX + "input").type(Type.RADIO)
          .name(name).value(clientType.ordinal()).id(clientType.name().toLowerCase())
          .onChange("onSelectType()");

      Span span = span().addClass(REG_STYLE_TYPE_PREFIX + "text")
          .text(clientType.getCaption(constants));

      container.append(label().addClass(REG_STYLE_TYPE_PREFIX + "label").append(input, span));
    }

    return tr().id(name + ID_SUFFIX_FIELD).append(
        registrationLabelCell(name + ID_SUFFIX_LABEL, constants.ecClientType(), true),
        td().addClass(REG_STYLE_INPUT_CELL).append(container));
  }

  private static Node registrationField(String label, String name, boolean required) {
    return registrationField(label, Type.TEXT, name, required);
  }

  private static Node registrationField(String label, Type type, String name, boolean required) {
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

  @Override
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    String html;
    String path = req.getPathInfo();

    if (BeeUtils.isEmpty(path)) {
      html = getInitialPage(req, UserInterface.E_COMMERCE);

    } else if (BeeUtils.same(path, PATH_REGISTER)) {
      String language = HttpUtils.getLanguage(req);
      LocalizableConstants constants = Localizations.getPreferredConstants(language);
      Map<String, String> parameters = HttpUtils.getParameters(req, false);

      if (parameters.size() > 3) {
        html = register(req, parameters, constants);
      } else {
        html = getRegistrationForm(req.getServletContext().getContextPath(), constants);
      }

    } else {
      HttpUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, path);
      return;
    }
    HttpUtils.sendResponse(resp, html);
  }

  @Override
  protected Node getLoginExtension(HttpServletRequest req,
      LocalizableConstants localizableConstants) {

    String stylePrefix = "bee-SignIn-";
    String styleName = stylePrefix + "Command-container";
    Div commandContainer = div().addClass(styleName);

    commandContainer.append(form()
        .addClass(stylePrefix + "Command-Form-register")
        .methodPost()
        .action(req.getServletContext().getContextPath() + req.getServletPath() + PATH_REGISTER)
        .append(input().type(Type.SUBMIT).addClass(stylePrefix + "Register")
            .value(localizableConstants.loginCommandRegister())));

    return commandContainer;
  }

  private Node branchSelector(String label) {
    String name = COL_REGISTRATION_BRANCH;
    Select select = select().name(name).required();

    BeeRowSet branches = qs.getViewData(CommonsConstants.VIEW_BRANCHES);
    if (!DataUtils.isEmpty(branches)) {
      int index = branches.getColumnIndex(CommonsConstants.COL_BRANCH_NAME);

      for (BeeRow row : branches.getRows()) {
        select.append(option().value(row.getId()).text(row.getString(index)));
      }
    }

    return tr().id(name + ID_SUFFIX_FIELD).append(
        registrationLabelCell(name + ID_SUFFIX_LABEL, label, true),
        td().addClass(REG_STYLE_INPUT_CELL).append(select));
  }

  private Datalist getDataList(String tblName, String fldName) {
    String[] values = qs.getColumn(new SqlSelect().setDistinctMode(true)
        .addFields(tblName, fldName)
        .addFrom(tblName)
        .setWhere(SqlUtils.notNull(tblName, fldName))
        .addOrder(tblName, fldName));

    if (values == null || values.length <= 0) {
      return null;
    }

    Datalist datalist = datalist();

    for (String value : values) {
      datalist.append(option().value(value));
    }

    return datalist;
  }

  private String getRegistrationForm(String contextPath, LocalizableConstants constants) {
    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.ecRegistrationNew()),
        link().styleSheet(resource(contextPath, Paths.getStyleSheetPath("ecregistration"))),
        script().src(resource(contextPath, Paths.getScriptPath("ecregistration"))));

    Tbody fields = tbody().append(
        branchSelector(constants.branch()),
        clientTypeSelector(constants),
        registrationField(constants.ecClientCompanyName(), COL_REGISTRATION_COMPANY_NAME, true),
        registrationField(constants.ecClientCompanyCode(), COL_REGISTRATION_COMPANY_CODE, true),
        registrationField(constants.ecClientVatCode(), COL_REGISTRATION_VAT_CODE, true),
        registrationField(constants.ecClientFirstName(), COL_REGISTRATION_FIRST_NAME, true),
        registrationField(constants.ecClientLastName(), COL_REGISTRATION_LAST_NAME, true),
        registrationField(constants.ecClientPersonCode(), COL_REGISTRATION_PERSON_CODE, false),
        registrationField(constants.email(), Type.EMAIL, COL_REGISTRATION_EMAIL, true),
        registrationField(constants.phone(), Type.TEL, COL_REGISTRATION_PHONE, true),
        registrationField(constants.country(), COL_REGISTRATION_COUNTRY, false),
        registrationField(constants.city(), COL_REGISTRATION_CITY, true),
        registrationField(constants.address(), COL_REGISTRATION_ADDRESS, true),
        registrationField(constants.postIndex(), COL_REGISTRATION_POST_INDEX, true),
        registrationField(constants.ecClientActivity(), COL_REGISTRATION_ACTIVITY, false));

    doc.getBody().append(
        div().addClass(REG_STYLE_PREFIX + "panel").append(
            div().addClass(REG_STYLE_PREFIX + "caption").text(constants.ecRegistrationNew()),
            form().addClass(REG_STYLE_PREFIX + "form").methodPost().append(
                table().addClass(REG_STYLE_PREFIX + "table").append(fields),
                input().type(Type.SUBMIT).addClass(REG_STYLE_PREFIX + "submit")
                    .value(constants.ecRegister()))));

    Datalist cities = getDataList(CommonsConstants.TBL_CITIES, CommonsConstants.COL_CITY_NAME);
    if (cities != null) {
      String listId = COL_REGISTRATION_CITY + ID_SUFFIX_LIST;
      cities.id(listId);

      Element element = fields.queryId(COL_REGISTRATION_CITY + ID_SUFFIX_INPUT);
      if (element instanceof Input) {
        ((Input) element).list(listId);
        doc.getBody().append(cities);
      }
    }

    Datalist countries = getDataList(CommonsConstants.TBL_COUNTRIES,
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
      LocalizableConstants constants) {

    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.ecRegistrationNew()));

    SqlInsert si = new SqlInsert(TBL_REGISTRATIONS);
    int cnt = 0;

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      String text = BeeUtils.toString(++cnt) + "  "
          + BeeUtils.join(": ", entry.getKey(), entry.getValue());

      doc.getBody().append(div().text(text), br());

      if (!BeeUtils.isEmpty(entry.getValue()) && proxy.isField(TBL_REGISTRATIONS, entry.getKey())) {
        si.addConstant(entry.getKey(), entry.getValue());
      }
    }

    doc.getBody().append(hr());

    si.addConstant(COL_REGISTRATION_DATE, TimeUtils.nowMinutes());
    si.addConstant(COL_REGISTRATION_HOST, req.getRemoteAddr());
    si.addConstant(COL_REGISTRATION_AGENT, req.getHeader(HttpHeaders.USER_AGENT));

    // ResponseObject response = proxy.insert(si);
    ResponseObject response = ResponseObject.response(si);
    if (response.hasErrors()) {
      for (String message : response.getErrors()) {
        doc.getBody().append(div().text(message));
      }

    } else {
      doc.getBody().append(div().text("OK"));
    }

    return doc.build();
  }
}
