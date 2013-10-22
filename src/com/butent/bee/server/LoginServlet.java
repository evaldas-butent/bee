package com.butent.bee.server;

import com.google.common.base.Strings;
import com.google.common.net.MediaType;

import static com.butent.bee.shared.html.builder.Factory.*;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.HttpConst;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.html.builder.elements.Form;
import com.butent.bee.shared.html.builder.elements.Input.Type;
import com.butent.bee.shared.html.builder.elements.Meta;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.SupportedLocale;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.ui.UserInterface;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.File;
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

  public enum State {
    EMPTY, SUCCESS, FAIL, BLOCK
  }

  public static final String URL = "/index.html";

  private static BeeLogger logger = LogUtils.getLogger(LoginServlet.class);

  private static String getBee(UserInterface ui, SupportedLocale locale) {

    Document doc = new Document();

    doc.getHead().append(meta().encodingDeclarationUtf8());

    if (locale != null) {
      doc.getHead().append(meta().name("gwt:property").content("locale=" + locale.getLanguage()));
    }

    List<Meta> meta = ui.getMeta();
    if (!BeeUtils.isEmpty(meta)) {
      doc.getHead().append(meta);
    }

    doc.getHead().append(
        title().text(ui.getTitle()),
        base().targetBlank());

    for (String styleSheet : ui.getStyleSheets()) {
      doc.getHead().append(link().styleSheet(resource(Paths.getStyleSheetPath(styleSheet))));
    }
    for (String script : ui.getScripts()) {
      doc.getHead().append(script().src(resource(Paths.getScriptPath(script))));
    }

    doc.getHead().append(script().src(resource("bee/bee.nocache.js")));

    return doc.build(0, 0);
  }

  public static String getForm(String userName, State state, Map<String, String> parameters,
      LocalizableConstants localizableConstants) {

    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text("to BEE or not to BEE"),
        link().styleSheet(resource(Paths.getStyleSheetPath("login"))),
        script().src(resource(Paths.getScriptPath("login"))));

    String stylePrefix = "bee-SignIn-";

    Div panel = div().addClass(stylePrefix + "Panel");
    doc.getBody().append(panel);

    Form form = form().addClass(stylePrefix + "Form").methodPost();

    form.append(
        div().addClass(stylePrefix + "Logo-container").append(
            img().addClass(stylePrefix + "Logo").src("images/logo.png").alt("logo")));

    if (SupportedLocale.values().length > 1) {
      Div localeContainer = div().addClass(stylePrefix + "Locale-container");

      for (SupportedLocale locale : SupportedLocale.values()) {
        localeContainer.append(
            label().addClass(stylePrefix + "Locale-label").append(
                input().addClass(stylePrefix + "Locale-input").type(Type.RADIO)
                    .name(HttpConst.PARAM_LOCALE).value(locale.getLanguage()),
                img().addClass(stylePrefix + "Locale-flag").title(locale.getCaption())
                    .src(Paths.getLangIconPath(locale.getIconName())).alt(locale.getCaption())));
      }

      form.append(localeContainer);
    }

    form.append(
        div().addClass(stylePrefix + "Label").addClass(stylePrefix + "Label-user")
            .text(localizableConstants.loginUserName()),
        input().addClass(stylePrefix + "Input").addClass(stylePrefix + "Input-user")
            .name(HttpConst.PARAM_USER).id("user").value(Strings.emptyToNull(userName))
            .onKeyDown("return goPswd(event)").autofocus().required(),
        div().addClass(stylePrefix + "Label").addClass(stylePrefix + "Label-password")
            .text(localizableConstants.loginPassword()),
        input().addClass(stylePrefix + "Input").addClass(stylePrefix + "Input-password")
            .type(Type.PASSWORD).name(HttpConst.PARAM_PASSWORD).id("pswd").required()
        );

    String ui = parameters.get(HttpConst.PARAM_UI);
    if (!BeeUtils.isEmpty(ui)) {
      form.append(input().type(Type.HIDDEN).name(HttpConst.PARAM_UI).value(ui));
    }

    if (state == State.FAIL) {
      form.append(div().addClass(stylePrefix + "Error").text(localizableConstants.loginFailed()));
    }

    form.append(input().type(Type.SUBMIT).addClass(stylePrefix + "Button").value(
        localizableConstants.loginSubmit()));

    panel.append(form);

    String commandRegister = parameters.get(HttpConst.PARAM_REGISTER);
    String commandQuery = parameters.get(HttpConst.PARAM_QUERY);

    if (!BeeUtils.allEmpty(commandRegister, commandQuery)) {
      String styleName = stylePrefix + "Command-container";
      Div commandContainer = div().addClass(BeeUtils.joinWords(styleName,
          BeeUtils.join(BeeConst.STRING_MINUS, styleName, commandRegister, commandQuery)));

      if (!BeeUtils.isEmpty(commandRegister)) {
        commandContainer.append(
            form().addClass(stylePrefix + "Command-Form-register").methodPost()
                .action(BeeUtils.trim(commandRegister)).append(
                    input().type(Type.SUBMIT).addClass(stylePrefix + "Register")
                        .value(localizableConstants.loginCommandRegister())));

      }

      if (!BeeUtils.isEmpty(commandQuery)) {
        commandContainer.append(
            form().addClass(stylePrefix + "Command-Form-query").methodPost()
                .action(BeeUtils.trim(commandQuery)).append(
                    input().type(Type.SUBMIT).addClass(stylePrefix + "Query")
                        .value(localizableConstants.loginCommandQuery())));
      }

      panel.append(commandContainer);
    }

    String wtfplUrl = UiConstants.wtfplUrl();

    panel.append(
        div().addClass(stylePrefix + "Copyright").title(wtfplUrl)
            .onClick("window.open('" + wtfplUrl + "')")
            .append(
                img().addClass(stylePrefix + "Copyright-logo").src(UiConstants.wtfplLogo())
                    .alt("wtfpl"),
                span().addClass(stylePrefix + "Copyright-label").text(UiConstants.wtfplLabel())));

    return doc.build(0, 2);
  }

  private static String resource(String path) {
    File file = new File(path);
    if (!file.exists()) {
      file = new File(Config.WAR_DIR, path);
      if (!file.exists()) {
        return path;
      }
    }

    long time = file.lastModified();
    if (time > 0) {
      return path + "?v=" + new DateTime(time).toTimeStamp();
    } else {
      return path;
    }
  }

  private static String verboten() {
    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text("Verboten"));

    doc.getBody().append(img().src("images/answer.jpg").alt("respect my authoritah"));

    return doc.build();
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
    String remoteUser = req.getRemoteUser();
    State state = (remoteUser == null) ? State.FAIL : State.SUCCESS;

    final String html;

    if (isBlocked(remoteUser)) {
      try {
        req.logout();
      } catch (ServletException e) {
        logger.error(e);
      }
      html = verboten();

    } else {
      Map<String, String> parameters = HttpUtils.getParameters(req, false);

      String language = BeeUtils.trim(parameters.get(HttpConst.PARAM_LOCALE));
      SupportedLocale userLocale = getUserLocale(remoteUser);

      if (state == State.SUCCESS) {
        if (!BeeUtils.isEmpty(language)) {
          SupportedLocale loginLocale = SupportedLocale.getByLanguage(language);
          if (loginLocale != null && loginLocale != userLocale) {
            userService.updateUserLocale(remoteUser, loginLocale);
            userLocale = loginLocale;
          }
        }

        UserInterface userInterface = null;

        String ui = BeeUtils.trim(parameters.get(HttpConst.PARAM_UI));
        if (!BeeUtils.isEmpty(ui)) {
          userInterface = UserInterface.getByShortName(ui);
        }
        if (userInterface == null) {
          userInterface = BeeUtils.nvl(getUserInterface(remoteUser), UserInterface.DEFAULT);
        }

        html = getBee(userInterface, userLocale);

      } else {
        if (BeeUtils.isEmpty(language)) {
          language = (userLocale == null) ? HttpUtils.getLanguage(req) : userLocale.getLanguage();
        }
        LocalizableConstants localizableConstants = Localizations.getPreferredConstants(language);

        html = getForm(remoteUser, state, parameters, localizableConstants);
      }
    }

    resp.setContentType(MediaType.HTML_UTF_8.toString());

    PrintWriter writer;
    try {
      writer = resp.getWriter();
      writer.print(html);
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
