package com.butent.bee.server.communication;

import com.butent.bee.server.Config;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Singleton
@Startup
@Lock(LockType.READ)
public class MailerBean {

  private static Logger logger = Logger.getLogger(MailerBean.class.getName());

  private static final String PROPERTY_MAILER = "MailerName";
  private static final String[] MAIL_PROPERTIES = new String[] {
      "mail.host",
      "mail.from",
      "mail.user",
      "mail.store.protocol",
      "mail.transport.protocol",
      "mail.smtp.host",
      "mail.smtp.user",
      "mail.debug"
  };
  Session session = null;

  public ResponseObject sendMail(String to, String subject, String body) {
    ResponseObject response;

    if (session == null) {
      response = ResponseObject.error("Mailer session not available");
    } else {
      MimeMessage message = new MimeMessage(session);

      try {
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, BeeConst.CHARSET_UTF8);
        message.setText(body, BeeConst.CHARSET_UTF8);

        Transport.send(message);
        response = ResponseObject.response("Mail sent");

      } catch (MessagingException ex) {
        LogUtils.severe(logger, ex);
        response = ResponseObject.error(ex);
      }
    }
    return response;
  }

  @PostConstruct
  private void init() {
    String mailer = Config.getProperty(PROPERTY_MAILER);

    if (!BeeUtils.isEmpty(mailer)) {
      try {
        try {
          session = (Session) InitialContext.doLookup("mail/" + mailer);
        } catch (NamingException ex) {
          try {
            session = (Session) InitialContext.doLookup("java:mail/" + mailer);
          } catch (NamingException ex2) {
            session = null;
          }
        }
      } catch (ClassCastException ex) {
        session = null;
        LogUtils.severe(logger, "Not a mail session:", BeeUtils.bracket(mailer));
      }
    }
    if (session == null) {
      Properties props = new Properties();

      for (String prop : MAIL_PROPERTIES) {
        String prp = Config.getProperty(prop);

        if (prp != null) {
          props.setProperty(prop, prp);
        }
      }
      session = Session.getInstance(props, null);
    }
  }
}
