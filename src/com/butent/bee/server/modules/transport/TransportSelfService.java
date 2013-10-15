package com.butent.bee.server.modules.transport;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.server.LoginServlet;
import com.butent.bee.server.ProxyBean;
import com.butent.bee.server.http.HttpConst;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TransportSelfService extends HttpServlet {

  private static final String PATH_REGISTER = "tr/register";
  private static final String PATH_REQUEST = "tr/request";

  private static BeeLogger logger = LogUtils.getLogger(TransportSelfService.class);

  private static List<String> getQueryForm(LocalizableConstants constants) {
    List<String> html = startHtml(constants.no());

    html.add("not yet");

    html.add("</body>");
    html.add("</html>");

    return html;
  }
  
  private static List<String> getRegistrationForm(LocalizableConstants constants) {
    List<String> html = startHtml(constants.trRegistrationNew());

    html.add("<h2>" + constants.trRegistrationFormCaption() + "</h2>");

    html.add("<form method=\"post\" action=\"tr/register?submit\">");
    html.add("<table style=\"border-collapse: collapse; margin-top: 2ex; margin-left: 1em;\">");
    html.add("<tbody>");

    String cellStyle = "vertical-align: top; padding-bottom: 4px;";
    String labelCellStyle = "text-align: right; padding-left: 1em; padding-right: 1em;";

    html.add("<tr>");
    renderField(html, constants.trRegistrationCompanyName(), COL_REGISTRATION_COMPANY_NAME,
        cellStyle, labelCellStyle, true);
    renderField(html, constants.trRegistrationCompanyCode(), COL_REGISTRATION_COMPANY_CODE,
        cellStyle, labelCellStyle, true);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationAddress(), COL_REGISTRATION_ADDRESS,
        cellStyle, labelCellStyle, true);
    renderField(html, constants.trRegistrationVatCode(), COL_REGISTRATION_VAT_CODE,
        cellStyle, labelCellStyle, false);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationCity(), COL_REGISTRATION_CITY,
        cellStyle, labelCellStyle, true);
    renderField(html, constants.trRegistrationCountry(), COL_REGISTRATION_COUNTRY,
        cellStyle, labelCellStyle, true);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationContact(), COL_REGISTRATION_CONTACT,
        cellStyle, labelCellStyle, true);
    renderField(html, constants.trRegistrationBank(), COL_REGISTRATION_BANK,
        cellStyle, labelCellStyle, false);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationContactPosition(), COL_REGISTRATION_CONTACT_POSITION,
        cellStyle, labelCellStyle, true);
    renderField(html, constants.trRegistrationBankAddress(), COL_REGISTRATION_BANK_ADDRESS,
        cellStyle, labelCellStyle, false);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationPhone(), COL_REGISTRATION_PHONE,
        cellStyle, labelCellStyle, true);
    renderField(html, constants.trRegistrationBankAccount(), COL_REGISTRATION_BANK_ACCOUNT,
        cellStyle, labelCellStyle, false);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationEmail(), COL_REGISTRATION_EMAIL,
        cellStyle, labelCellStyle, true);
    renderField(html, constants.trRegistrationMobile(), COL_REGISTRATION_MOBILE,
        cellStyle, labelCellStyle, false);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationExchangeCode(), COL_REGISTRATION_EXCHANGE_CODE,
        cellStyle, labelCellStyle, false);
    renderField(html, constants.trRegistrationFax(), COL_REGISTRATION_FAX,
        cellStyle, labelCellStyle, false);
    html.add("</tr>");

    html.add("<tr>");
    renderField(html, constants.trRegistrationSwift(), COL_REGISTRATION_SWIFT,
        cellStyle, labelCellStyle, false);
    renderField(html, constants.trRegistrationNotes(), COL_REGISTRATION_NOTES,
        cellStyle, labelCellStyle, false);
    html.add("</tr>");

    html.add("</tbody>");
    html.add("</table>");

    html.add("<input type=\"submit\" value=\"" + constants.trRegistrationActionSubmit() + "\" />");
    html.add("</form>");

    html.add("</body>");
    html.add("</html>");

    return html;
  }

  private static void renderField(List<String> html, String label, String source,
      String cellStyle, String labelCellStyle, boolean required) {

    StringBuilder sb = new StringBuilder();
    sb.append("<td style=\"" + BeeUtils.joinWords(cellStyle, labelCellStyle) + "\"><div");
    if (required) {
      sb.append(" class=\"bee-required\"");
    }
    sb.append(">" + label + "</div></td>");

    html.add(sb.toString());

    sb = new StringBuilder();
    sb.append("<td style=\"" + cellStyle + "\">");
    sb.append("<input type=\"text\" name=\"" + source + "\"");
    if (required) {
      sb.append(" required");
    }
    sb.append(" /></td>");

    html.add(sb.toString());
  }

  private static List<String> startHtml(String title) {
    List<String> html = Lists.newArrayList();

    html.add("<!doctype html>");
    html.add("<html>");
    html.add("<head>");
    html.add("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\" />");

    if (!BeeUtils.isEmpty(title)) {
      html.add("<title>" + title + "</title>");
    }

    html.add("<style>");
    html.add(".bee-required:after {");
    html.add("content: \"*\"; color: red; font-size: 13px; font-weight: bold;");
    html.add("}");
    html.add("</style>");

    html.add("</head>");
    html.add("<body>");

    return html;
  }

  @EJB
  ProxyBean transactionService;

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

  private void doService(HttpServletRequest req, HttpServletResponse resp) {
    String uri = req.getRequestURI();

    if (!BeeUtils.containsAnySame(uri, PATH_REGISTER, PATH_REQUEST)) {
      String forwardPath = CommUtils.getPath(LoginServlet.URL,
          ImmutableMap.of(HttpConst.PARAM_UI, UserInterface.SELF_SERVICE.getShortName(),
              HttpConst.PARAM_REGISTER, PATH_REGISTER,
              HttpConst.PARAM_QUERY, PATH_REQUEST), false);

      RequestDispatcher dispatcher = req.getRequestDispatcher(forwardPath);

      if (dispatcher != null) {
        try {
          dispatcher.forward(req, resp);

        } catch (ServletException ex) {
          logger.error(ex);
        } catch (IOException ex) {
          logger.error(ex);
        }
      }
      return;
    }

    String language = HttpUtils.getLanguage(req);
    LocalizableConstants constants = Localizations.getPreferredConstants(language);

    List<String> html;

    if (BeeUtils.containsSame(uri, PATH_REGISTER)) {
      Map<String, String> parameters = HttpUtils.getParameters(req, false);

      if (parameters.size() > 3) {
        html = register(req, parameters, constants);
      } else {
        html = getRegistrationForm(constants);
      }
    } else {
      html = getQueryForm(constants);
    }

    resp.setContentType(MediaType.HTML_UTF_8.toString());

    PrintWriter writer;
    try {
      writer = resp.getWriter();
      writer.print(BeeUtils.buildLines(html));
      writer.flush();
    } catch (IOException ex) {
      logger.error(ex);
    }
  }

  private List<String> register(HttpServletRequest req, Map<String, String> parameters,
      LocalizableConstants constants) {

    List<String> html = startHtml(constants.trRegistrationNew());

    SqlInsert si = new SqlInsert(TBL_REGISTRATIONS);
    int cnt = 0;

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      html.add(BeeUtils.toString(++cnt) + "  "
          + BeeUtils.join(": ", entry.getKey(), entry.getValue()));
      html.add("<br/>");
      
      if (!BeeUtils.isEmpty(entry.getValue())) {
        si.addConstant(entry.getKey(), entry.getValue());
      }
    }

    html.add("<hr/>");
    
    si.addConstant(COL_REGISTRATION_DATE, TimeUtils.nowMinutes());
    si.addConstant(COL_REGISTRATION_HOST, req.getRemoteAddr());
    si.addConstant(COL_REGISTRATION_AGENT, req.getHeader(HttpHeaders.USER_AGENT));
    
    ResponseObject response = transactionService.insert(si);
    if (response.hasErrors()) {
      for (String message : response.getErrors()) {
        html.add(message);
        html.add("<br/>");
      }

    } else {
      html.add("OK");
    }

    html.add("</body>");
    html.add("</html>");

    return html;
  }
}
