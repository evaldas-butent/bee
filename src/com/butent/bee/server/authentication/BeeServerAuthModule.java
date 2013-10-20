package com.butent.bee.server.authentication;

import com.google.common.net.MediaType;

import com.butent.bee.server.LoginServlet;
import com.butent.bee.server.LoginServlet.State;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.HttpConst;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.i18n.Localizations;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BeeServerAuthModule implements ServerAuthModule {

  private static BeeLogger logger = LogUtils.getLogger(BeeServerAuthModule.class);

  private static final String POLICY = MessagePolicy.class.getName() + ".isMandatory";

  private CallbackHandler callbackHandler;
  private final Class<?>[] supportedMessageTypes = new Class[] {HttpServletRequest.class,
      HttpServletResponse.class};

  @Override
  public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
  }

  @Override
  public Class<?>[] getSupportedMessageTypes() {
    return supportedMessageTypes;
  }

  @Override
  public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy,
      CallbackHandler handler, @SuppressWarnings("rawtypes") Map options)
      throws AuthException {

    this.callbackHandler = handler;
  }

  @Override
  public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject)
      throws AuthException {
    return AuthStatus.SEND_SUCCESS;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject,
      Subject serviceSubject) throws AuthException {

    if (!Boolean.parseBoolean((String) messageInfo.getMap().get(POLICY))) {
      CallerPrincipalCallback callerPrincipalCallback =
          new CallerPrincipalCallback(clientSubject, (Principal) null);

      return handleCallbacks(new Callback[] {callerPrincipalCallback});
    }
    HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
    Principal userPrincipal = request.getUserPrincipal();

    if (userPrincipal != null) {
      // Execute protocol to signal container remember authentication session be used.
      CallerPrincipalCallback callerPrincipalCallback =
          new CallerPrincipalCallback(clientSubject, userPrincipal);

      return handleCallbacks(new Callback[] {callerPrincipalCallback});
    }
    Map<String, String> parameters = HttpUtils.getParameters(request, false);

    String userName = BeeUtils.trim(parameters.get(HttpConst.PARAM_USER));
    String password = BeeUtils.trim(parameters.get(HttpConst.PARAM_PASSWORD));
    boolean ok = false;

    if (!BeeUtils.anyEmpty(userName, password)) {
      String mod = UserServiceBean.class.getSimpleName();
      UserServiceBean usr;

      try {
        usr = (UserServiceBean) InitialContext.doLookup("java:global"
            + BeeUtils.join("/", request.getServletContext().getContextPath(), mod));

        ok = usr.authenticateUser(userName, Codec.md5(BeeUtils.trim(password)));
      } catch (NamingException ex) {
        logger.severe("Module not found:", BeeUtils.bracket(mod));
      } catch (ClassCastException ex) {
        logger.severe("Not a module:", BeeUtils.bracket(mod));
      }
    }
    if (!ok) {
      String html = LoginServlet.getForm(userName, BeeUtils.isEmpty(userName) ? null : State.FAIL,
          parameters, Localizations.getPreferredConstants(HttpUtils.getLanguage(request)));

      HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
      response.setContentType(MediaType.HTML_UTF_8.toString());

      PrintWriter writer;
      try {
        writer = response.getWriter();
        writer.print(html);
        writer.flush();
      } catch (IOException ex) {
        logger.error(ex);
      }
      return AuthStatus.SEND_CONTINUE;
    }
    // Tell container to register an authentication session.
    messageInfo.getMap().put("javax.servlet.http.registerSession", Boolean.TRUE.toString());

    CallerPrincipalCallback callerPrincipalCallback =
        new CallerPrincipalCallback(clientSubject, userName);
    GroupPrincipalCallback groupPrincipalCallback =
        new GroupPrincipalCallback(clientSubject, new String[] {"architect"});

    return handleCallbacks(new Callback[] {callerPrincipalCallback, groupPrincipalCallback});
  }

  private AuthStatus handleCallbacks(Callback[] callbacks) throws AuthException {
    try {
      callbackHandler.handle(callbacks);
    } catch (IOException | UnsupportedCallbackException e) {
      throw (AuthException) new AuthException().initCause(e);
    }
    return AuthStatus.SUCCESS;
  }
}
