package com.butent.bee.server;

import com.google.common.collect.Lists;
import com.google.common.net.MediaType;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.HttpConst;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.io.FileNameUtils;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

  private enum State {
    EMPTY, SUCCES, FAIL, BLOCK
  }

  public static final String URL = "/index.html";

  private static final String CSS_DIR = "css";
  private static final String CSS_EXT = "css";

  private static final String JS_DIR = "js";
  private static final String JS_EXT = "js";

  private static BeeLogger logger = LogUtils.getLogger(LoginServlet.class);

  private static List<String> getBee(UserInterface ui, SupportedLocale locale) {
    List<String> html = startHtml();

    if (locale != null) {
      html.add("<meta name=\"gwt:property\" content=\"locale=" + locale.getLanguage() + "\" />");
    }

    Map<String, String> meta = ui.getMeta();
    if (!BeeUtils.isEmpty(meta)) {
      for (Map.Entry<String, String> entry : meta.entrySet()) {
        html.add("<meta name=\"" + entry.getKey() + "\" content=\"" + entry.getValue() + "\" />");
      }
    }

    html.add("<title>" + ui.getTitle() + "</title>");
    html.add("<base target=\"_blank\" />");

    for (String styleSheet : ui.getStyleSheets()) {
      html.add(getStyleSheetRef(styleSheet));
    }

    for (String script : ui.getScripts()) {
      html.add(getScriptRef(script));
    }

    html.add(getScriptRef("bee/bee.nocache.js"));

    html.add("</head>");

    html.add("<body>");
    html.add("</body>");
    html.add("</html>");

    return html;
  }

  private static List<String> getForm(String userName, State state, Map<String, String> parameters,
      LocalizableConstants localizableConstants) {

    List<String> html = startHtml();
    html.add("<title>to BEE or not to BEE</title>");

    html.add(getStyleSheetRef("login"));
    html.add(getScriptRef("login"));

    html.add("</head>");
    html.add("<body>");

    html.add("<div class=\"bee-SignIn-Panel\">");
    html.add("<form class=\"bee-SignIn-Form\" method=\"post\">");

    if (SupportedLocale.values().length > 1) {
      html.add("<div class=\"bee-SignIn-Locale-container\">");

      for (SupportedLocale locale : SupportedLocale.values()) {
        html.add("<label><input type=\"radio\" name=\"" + HttpConst.PARAM_LOCALE
            + "\" value=\"" + locale.getLanguage() + "\" />" + locale.getCaption() + "</label>");
      }

      html.add("</div>");
    }

    html.add("<div class=\"bee-SignIn-Logo-container\">");
    html.add("<img class=\"bee-SignIn-Logo\" src=\"images/logo.png\" />");
    html.add("</div>");

    String text = (localizableConstants == null) ? "Prisijungimo vardas"
        : localizableConstants.loginUserName();
    html.add("<div class=\"bee-SignIn-Label bee-SignIn-Label-user\">" + text + "</div>");

    StringBuilder sb = new StringBuilder();
    sb.append("<input type=\"text\" class=\"bee-SignIn-Input bee-SignIn-Input-user\" name=\"");
    sb.append(HttpConst.PARAM_USER + "\" id=\"user\"");
    if (!BeeUtils.isEmpty(userName)) {
      sb.append(" value=\"" + BeeUtils.trim(userName) + "\"");
    }
    sb.append(" onkeydown=\"return goPswd(event)\" autofocus required />");
    html.add(sb.toString());

    text = (localizableConstants == null) ? "Slaptažodis" : localizableConstants.loginPassword();
    html.add("<div class=\"bee-SignIn-Label bee-SignIn-Label-password\">" + text + "</div>");

    html.add("<input type=\"password\" class=\"bee-SignIn-Input bee-SignIn-Input-password\""
        + "name=\"" + HttpConst.PARAM_PASSWORD + "\" id=\"pswd\" required />");

    String ui = parameters.get(HttpConst.PARAM_UI);
    if (!BeeUtils.isEmpty(ui)) {
      html.add("<input type=\"hidden\" name=\"" + HttpConst.PARAM_UI + "\" value=\""
          + BeeUtils.trim(ui) + "\" />");
    }

    text = (localizableConstants == null) ? "Prisijungti" : localizableConstants.loginSubmit();
    html.add("<input type=\"submit\" class=\"bee-SignIn-Button\" value=\"" + text + "\" />");

    if (state == State.FAIL) {
      text = (localizableConstants == null) ? "Bandykite dar kartą"
          : localizableConstants.loginFailed();
      html.add("<div class=\"bee-SignIn-Error\">" + text + "</div>");
    }
    html.add("</form>");

    String commandRegister = parameters.get(HttpConst.PARAM_REGISTER);
    String commandQuery = parameters.get(HttpConst.PARAM_QUERY);

    if (!BeeUtils.allEmpty(commandRegister, commandQuery)) {
      String styleName = "bee-SignIn-Command-container";
      html.add("<div class=\"" + BeeUtils.joinWords(styleName,
          BeeUtils.join(BeeConst.STRING_MINUS, styleName, commandRegister, commandQuery)) + "\">");

      if (!BeeUtils.isEmpty(commandRegister)) {
        html.add("<form class=\"bee-SignIn-Command-Form-register\" method=\"post\" action=\""
            + BeeUtils.trim(commandRegister) + "\">");

        text = (localizableConstants == null) ? "Registruotis"
            : localizableConstants.loginCommandRegister();
        html.add("<input type=\"submit\" class=\"bee-SignIn-Register\" value=\"" + text + "\" />");

        html.add("</form>");
      }

      if (!BeeUtils.isEmpty(commandQuery)) {
        html.add("<form class=\"bee-SignIn-Command-Form-query\" method=\"post\" action=\""
            + BeeUtils.trim(commandQuery) + "\">");

        text = (localizableConstants == null) ? "Pateikti užklausą"
            : localizableConstants.loginCommandQuery();
        html.add("<input type=\"submit\" class=\"bee-SignIn-Query\" value=\"" + text + "\" />");

        html.add("</form>");
      }

      html.add("</div>");
    }

    html.add("<div class=\"bee-SignIn-Caption\">");
    html.add("<img class=\"bee-Copyright-logo\" src=\"images/logo.gif\" />");
    html.add("<span>UAB \"Būtenta\" &copy; 2010 - " + TimeUtils.today().getYear() + "</span>");
    html.add("</div>");
    html.add("</div>");

    html.add("</body>");
    html.add("</html>");

    return html;
  }

  private static String getScriptRef(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return null;
    } else {
      return "<script src=\"" + normalize(JS_DIR, fileName, JS_EXT) + "\"></script>";
    }
  }

  private static String getStyleSheetRef(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return null;
    } else {
      return "<link rel=\"stylesheet\" href=\"" + normalize(CSS_DIR, fileName, CSS_EXT) + "\" />";
    }
  }

  private static String normalize(String dir, String name, String ext) {
    if (FileNameUtils.hasSeparator(name)) {
      return FileNameUtils.defaultExtension(name, ext);
    } else {
      return dir + String.valueOf(FileNameUtils.UNIX_SEPARATOR)
          + FileNameUtils.defaultExtension(name, ext);
    }
  }

  private static List<String> startHtml() {
    List<String> html = Lists.newArrayList();

    html.add("<!doctype html>");
    html.add("<html>");
    html.add("<head>");
    html.add("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\" />");

    return html;
  }

  private static List<String> verboten() {
    List<String> html = startHtml();
    html.add("<title>Verboten</title>");

    html.add("</head>");
    html.add("<body>");

    html.add("<img src=\"images/answer.jpg\" />");

    html.add("</body>");
    html.add("</html>");

    return html;
  }

  @EJB
  UserServiceBean userService;

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
    Map<String, String> parameters = HttpUtils.getParameters(req, false);
    logger.debug("login", parameters, req.getSession().getId());

    String userName = BeeUtils.trim(parameters.get(HttpConst.PARAM_USER));
    String password = BeeUtils.trim(parameters.get(HttpConst.PARAM_PASSWORD));

    State state;

    if (BeeUtils.anyEmpty(userName, password)) {
      state = State.EMPTY;

    } else if (isBlocked(userName)) {
      state = State.BLOCK;

    } else {
      try {
        String remoteUser = req.getRemoteUser();
        if (remoteUser != null) {
          req.logout();
          logger.info("logout", remoteUser);
        }

        req.login(userName, password);
        remoteUser = req.getRemoteUser();

        logger.debug("login", remoteUser);
        state = (remoteUser == null) ? State.FAIL : State.SUCCES;

      } catch (ServletException ex) {
        logger.severe("login", userName, password);
        logger.error(ex);

        state = State.FAIL;
      }
    }

    final List<String> html;

    if (state == State.BLOCK) {
      html = verboten();

    } else {
      String language = BeeUtils.trim(parameters.get(HttpConst.PARAM_LOCALE));
      SupportedLocale userLocale = getUserLocale(userName);

      if (state == State.SUCCES) {
        if (!BeeUtils.isEmpty(language)) {
          SupportedLocale loginLocale = SupportedLocale.getByLanguage(language);
          if (loginLocale != null && loginLocale != userLocale) {
            userService.updateUserLocale(userName, loginLocale);
            userLocale = loginLocale;
          }
        }

        UserInterface userInterface = null;

        String ui = BeeUtils.trim(parameters.get(HttpConst.PARAM_UI));
        if (!BeeUtils.isEmpty(ui)) {
          userInterface = UserInterface.getByShortName(ui);
        }
        if (userInterface == null) {
          userInterface = BeeUtils.nvl(getUserInterface(userName), UserInterface.DEFAULT);
        }

        html = getBee(userInterface, userLocale);

      } else {
        if (BeeUtils.isEmpty(language)) {
          language = (userLocale == null) ? HttpUtils.getLanguage(req) : userLocale.getLanguage();
        }
        LocalizableConstants localizableConstants = Localizations.getPreferredConstants(language);

        html = getForm(userName, state, parameters, localizableConstants);
      }
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

  private UserInterface getUserInterface(String userName) {
    return userService.getUserInterface(userName);
  }

  private SupportedLocale getUserLocale(String userName) {
    return userService.isUser(userName) ? userService.getUserLocale(userName) : null;
  }

  private boolean isBlocked(String userName) {
    return !BeeUtils.isEmpty(userName) && userService.isUser(userName)
        && BeeUtils.isTrue(userService.isBlocked(userName));
  }
}
