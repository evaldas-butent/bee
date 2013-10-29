package com.butent.bee.server.modules.ec;

import com.google.common.net.HttpHeaders;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.server.LoginServlet;
import com.butent.bee.server.ProxyBean;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.Node;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.html.builder.elements.Input.Type;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class EcServlet extends LoginServlet {

  private static final String PATH_REGISTER = "/register";

  private static String getRegistrationForm(LocalizableConstants constants) {
    String classRequired = "bee-required";
    String classTable = "bee-ec-registration-table";
    String classLabelCell = "bee-ec-registration-label-cell";

    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text(constants.ecRegistrationNew()),
        style()
            .text("." + classRequired + ":after {")
            .text("  content: \"*\"; color: red; font-size: 13px; font-weight: bold;")
            .text("}")
            .text("." + classTable + " td {")
            .text("  vertical-align: top;")
            .text("  padding-bottom: 4px;")
            .text("}")
            .text("." + classLabelCell + " {")
            .text("  text-align: right;")
            .text("  padding-left: 1em;")
            .text("  padding-right: 1em;")
            .text("}"));

    doc.getBody().append(
        h2().text(constants.ecRegistrationNew()),
        form().methodPost().append(
            table().borderCollapse().marginTop(2, CssUnit.EX).marginLeft(1, CssUnit.EM).append(
                tbody().append(
                    tr().append(
                        td().addClass(classLabelCell).append(
                            div().text(constants.ecClientCompanyName())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_COMPANY_NAME)),
                        td().addClass(classLabelCell).append(
                            div().text(constants.ecClientCompanyCode())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_COMPANY_CODE))
                        ),
                    tr().append(
                        td().addClass(classLabelCell).append(
                            div().text(constants.ecClientPersonCode())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_PERSON_CODE)),
                        td().addClass(classLabelCell).append(
                            div().text(constants.ecClientVatCode())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_VAT_CODE))
                        ),
                    tr().append(
                        td().addClass(classLabelCell).append(
                            div().addClass(classRequired).text(constants.ecClientFirstName())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_FIRST_NAME).required()),
                        td().addClass(classLabelCell).append(
                            div().addClass(classRequired).text(constants.ecClientLastName())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_LAST_NAME).required())
                        ),
                    tr().append(
                        td().addClass(classLabelCell).append(
                            div().addClass(classRequired).text(constants.email())),
                        td().append(
                            input().type(Type.EMAIL).name(COL_REGISTRATION_EMAIL).required()),
                        td().addClass(classLabelCell).append(
                            div().addClass(classRequired).text(constants.phone())),
                        td().append(
                            input().type(Type.TEL).name(COL_REGISTRATION_PHONE).required())
                        ),
                    tr().append(
                        td().addClass(classLabelCell).append(
                            div().addClass(classRequired).text(constants.city())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_CITY).required()),
                        td().addClass(classLabelCell).append(
                            div().text(constants.country())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_COUNTRY))
                        ),
                    tr().append(
                        td().addClass(classLabelCell).append(
                            div().addClass(classRequired).text(constants.address())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_ADDRESS).required()),
                        td().addClass(classLabelCell).append(
                            div().addClass(classRequired).text(constants.postIndex())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_POST_INDEX).required())
                        ),
                    tr().append(
                        td().addClass(classLabelCell).append(
                            div().text(constants.ecClientActivity())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_ACTIVITY)),
                        td().addClass(classLabelCell).append(
                            div().text(constants.notes())),
                        td().append(
                            input().type(Type.TEXT).name(COL_REGISTRATION_NOTES))
                        )
                    )),
            input().type(Type.SUBMIT).value("Submit")));

    return doc.build(0, 2);
  }

  @EJB
  ProxyBean proxy;

  @Override
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    String html;
    String path = req.getPathInfo();

    if (BeeUtils.same(path, PATH_REGISTER)) {
      String language = HttpUtils.getLanguage(req);
      LocalizableConstants constants = Localizations.getPreferredConstants(language);
      Map<String, String> parameters = HttpUtils.getParameters(req, false);

      if (parameters.size() > 3) {
        html = register(req, parameters, constants);
      } else {
        html = getRegistrationForm(constants);
      }
    } else if (BeeUtils.isEmpty(path)) {
      html = doDefault(req, UserInterface.E_COMMERCE);
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

    si.addConstant(COL_REGISTRATION_TYPE, 0);

    String branches = CommonsConstants.TBL_BRANCHES;
    si.addConstant(COL_REGISTRATION_BRANCH, proxy.getColumnValues(new SqlSelect().
        addFields(branches, proxy.getIdName(branches)).addFrom(branches))[0]);

    ResponseObject response = proxy.insert(si);
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
