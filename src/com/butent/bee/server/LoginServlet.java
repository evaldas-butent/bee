package com.butent.bee.server;

import com.google.common.base.Strings;

import static com.butent.bee.shared.html.builder.Factory.*;

import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.HttpConst;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.Node;
import com.butent.bee.shared.html.builder.elements.Div;
import com.butent.bee.shared.html.builder.elements.Form;
import com.butent.bee.shared.html.builder.elements.Input.Type;
import com.butent.bee.shared.html.builder.elements.Link.Rel;
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
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/index.html", "/index.htm", "/index.jsp" })
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

  private static BeeLogger logger = LogUtils.getLogger(LoginServlet.class);

  private static final String FAV_ICON = "favicon.ico";
  private static final String LOGO = "logo.png";

  protected static String resource(String contextPath, String path) {
    File file = new File(path);
    if (!file.exists()) {
      file = new File(Config.WAR_DIR, path);
      if (!file.exists()) {
        return path;
      }
    }
    String requestPath;

    if (BeeUtils.isEmpty(contextPath) || path.startsWith("/")) {
      requestPath = path;
    } else {
      requestPath = contextPath + "/" + path;
    }
    long time = file.lastModified();

    if (time > 0) {
      requestPath += "?v=" + new DateTime(time).toTimeStamp();
    }
    return requestPath;
  }

  private static String render(String contextPath, UserInterface ui, SupportedLocale locale) {
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
        link().rel(Rel.SHORTCUT_ICON)
            .href(resource(contextPath, Paths.getImagePath(LoginServlet.FAV_ICON))),
        base().targetBlank());

    for (String styleSheet : ui.getStyleSheets()) {
      doc.getHead().append(link()
          .styleSheet(resource(contextPath, Paths.getStyleSheetPath(styleSheet))));
    }
    for (String script : ui.getScripts()) {
      doc.getHead().append(script().src(resource(contextPath, Paths.getScriptPath(script))));
    }
    doc.getHead().append(script().src(resource(contextPath, "bee/bee.nocache.js")));

    return doc.build(0, 0);
  }

  private static String verboten(String contextPath) {
    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text("Verboten"));

    doc.getBody().append(img().src(resource(contextPath, Paths.getImagePath("answer.jpg")))
        .alt("respect my authoritah"));

    return doc.build();
  }

  @EJB
  UserServiceBean userService;

  public String getLoginForm(HttpServletRequest request, String userName) {
    String contextPath = request.getServletContext().getContextPath();
    LocalizableConstants localizableConstants =
        Localizations.getPreferredConstants(HttpUtils.getLanguage(request));

    Document doc = new Document();

    doc.getHead().append(
        meta().encodingDeclarationUtf8(),
        title().text("to BEE or not to BEE"),
        link().rel(Rel.SHORTCUT_ICON).href(resource(contextPath, Paths.getImagePath(FAV_ICON))),
        link().styleSheet(resource(contextPath, Paths.getStyleSheetPath("login"))),
        script().src(resource(contextPath, Paths.getScriptPath("login"))));

    String stylePrefix = "bee-SignIn-";

    Div panel = div().addClass(stylePrefix + "Panel");
    doc.getBody().append(panel);

    Form form = form().addClass(stylePrefix + "Form").methodPost();

    form.append(
        div().addClass(stylePrefix + "Logo-container").append(
            img().addClass(stylePrefix + "Logo")
                .src(resource(contextPath, Paths.getImagePath(LOGO))).alt("logo")));

    if (SupportedLocale.values().length > 1) {
      Div localeContainer = div().addClass(stylePrefix + "Locale-container");

      for (SupportedLocale locale : SupportedLocale.values()) {
        localeContainer.append(
            label().addClass(stylePrefix + "Locale-label").append(
                input().addClass(stylePrefix + "Locale-input").type(Type.RADIO)
                    .name(HttpConst.PARAM_LOCALE).value(locale.getLanguage()),
                img().addClass(stylePrefix + "Locale-flag").title(locale.getCaption())
                    .src(resource(contextPath, Paths.getLangIconPath(locale.getIconName())))
                    .alt(locale.getCaption())));
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

    if (!BeeUtils.isEmpty(userName)) {
      form.append(div().addClass(stylePrefix + "Error").text(localizableConstants.loginFailed()));
    }
    form.append(input().type(Type.SUBMIT).addClass(stylePrefix + "Button").value(
        localizableConstants.loginSubmit()));

    panel.append(form);

    Node extension = getLoginExtension(request, localizableConstants);

    if (extension != null) {
      panel.append(extension);
    }

    String wtfplUrl = UiConstants.wtfplUrl();

    panel.append(
        div().addClass(stylePrefix + "Copyright").title(wtfplUrl)
            .onClick("window.open('" + wtfplUrl + "')")
            .append(
                img().addClass(stylePrefix + "Copyright-logo")
                    .src(resource(contextPath, UiConstants.wtfplLogo())).alt("wtfpl"),
                span().addClass(stylePrefix + "Copyright-label").text(UiConstants.wtfplLabel())));

    return doc.build(0, 2);
  }

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

  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    HttpUtils.sendResponse(resp, getInitialPage(req, null));
  }

  protected String getInitialPage(HttpServletRequest req, UserInterface ui) {
    String remoteUser = req.getRemoteUser();
    String contextPath = req.getServletContext().getContextPath();
    final String html;

    if (isBlocked(remoteUser)) {
      try {
        req.logout();
      } catch (ServletException e) {
        logger.error(e);
      }
      html = verboten(contextPath);

    } else {
      String language = BeeUtils.trim(req.getParameter(HttpConst.PARAM_LOCALE));
      SupportedLocale userLocale = getUserLocale(remoteUser);

      if (!BeeUtils.isEmpty(language)) {
        SupportedLocale loginLocale = SupportedLocale.getByLanguage(language);

        if (loginLocale != null && loginLocale != userLocale) {
          userService.updateUserLocale(remoteUser, loginLocale);
          userLocale = loginLocale;
        }
      }
      html = render(contextPath, ui == null
          ? BeeUtils.nvl(userService.getUserInterface(remoteUser), UserInterface.DEFAULT)
          : ui, userLocale);
    }
    return html;
  }

  @SuppressWarnings("unused")
  protected Node getLoginExtension(HttpServletRequest req,
      LocalizableConstants localizableConstants) {
    return null;
  }

  private SupportedLocale getUserLocale(String userName) {
    return userService.isUser(userName) ? userService.getUserLocale(userName) : null;
  }

  private boolean isBlocked(String userName) {
    return !BeeUtils.isEmpty(userName) && userService.isUser(userName)
        && BeeUtils.isTrue(userService.isBlocked(userName));
  }
}
