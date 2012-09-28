package com.butent.bee.server.modules.mail;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.mail.MailConstants.MAIL_MODULE;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.mail.proxy.MailProxy;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Stateless
@LocalBean
public class MailModuleBean implements BeeModule {

  private static final BeeLogger logger = LogUtils.getLogger(MailModuleBean.class);

  @EJB
  MailProxy proxy;

  private final Session session = null;

  @Override
  public Collection<String> dependsOn() {
    return null;
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(MailConstants.MAIL_METHOD);

    if (BeeUtils.same(svc, MailConstants.SVC_RESTART_PROXY)) {
      response = proxy.initServer();
      response.log(logger);

    } else {
      String msg = BeeUtils.concat(1, "Mail service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    List<BeeParameter> params = Lists.newArrayList(
        new BeeParameter(MAIL_MODULE,
            "POP3Server", ParameterType.TEXT, "POP3 server name", false, null),
        new BeeParameter(MAIL_MODULE,
            "POP3ServerPort", ParameterType.NUMBER, "POP3 server port number", false, null),
        new BeeParameter(MAIL_MODULE,
            "POP3BindPort", ParameterType.NUMBER, "POP3 proxy port number to listen on", false,
            null),
        new BeeParameter(MAIL_MODULE,
            "SMTPServer", ParameterType.TEXT, "SMTP server name", false, null),
        new BeeParameter(MAIL_MODULE,
            "SMTPServerPort", ParameterType.NUMBER, "SMTP server port number", false, null),
        new BeeParameter(MAIL_MODULE,
            "SMTPBindPort", ParameterType.NUMBER, "SMTP proxy port number to listen on", false,
            null));

    return params;
  }

  @Override
  public String getName() {
    return MAIL_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    proxy.initServer();
  }

  public ResponseObject sendMail(String to, String subject, String body) {
    ResponseObject response;

    if (session == null) {
      String msg = "Mail session not available";
      logger.error(msg);
      response = ResponseObject.error(msg);
    } else {
      MimeMessage message = new MimeMessage(session);

      try {
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, BeeConst.CHARSET_UTF8);
        message.setText(body, BeeConst.CHARSET_UTF8);

        Transport.send(message);
        response = ResponseObject.response("Mail sent");

      } catch (MessagingException ex) {
        response = ResponseObject.error(ex);
      }
    }
    return response;
  }

  public void storeMail(String mail, Protocol protocol) {
    logger.info("Saved", protocol, mail);
  }
}
