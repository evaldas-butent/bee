package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.utils.BeeUtils;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class MailProxy {

  @EJB
  ParamHolderBean prm;
  @EJB
  MailModuleBean mail;

  private final BeeLogger logger = LogUtils.getLogger(getClass());

  private String pop3ServerName;
  private int pop3ServerPort;
  private int pop3BindPort;

  private String smtpServerName;
  private int smtpServerPort;
  private int smtpBindPort;

  private POP3ProtocolServer pop3Proxy;
  private SMTPProtocolServer smtpProxy;

  @Lock(LockType.WRITE)
  public ResponseObject initServer() {
    tearDownServer();
    ResponseObject response = new ResponseObject();

    if (initParameters(Protocol.POP3)) {
      pop3Proxy = new POP3ProtocolServer(this);
      response.addInfo("POP3 proxy started");
    } else {
      response.addWarning("POP3 proxy parameters missing");
    }
    if (initParameters(Protocol.SMTP)) {
      smtpProxy = new SMTPProtocolServer(this);
      response.addInfo("SMTP proxy started");
    } else {
      response.addWarning("SMTP proxy parameters missing");
    }
    return response;
  }

  Integer getBindPort(Protocol protocol) {
    Integer bindPort = null;

    switch (protocol) {
      case POP3:
        bindPort = pop3BindPort;
        break;
      case IMAP:
        Assert.notImplemented();
        break;
      case SMTP:
        bindPort = smtpBindPort;
        break;
    }
    return bindPort;
  }

  String getServerName(Protocol protocol) {
    String serverName = null;

    switch (protocol) {
      case POP3:
        serverName = pop3ServerName;
        break;
      case IMAP:
        Assert.notImplemented();
        break;
      case SMTP:
        serverName = smtpServerName;
        break;
    }
    return serverName;
  }

  Integer getServerPort(Protocol protocol) {
    Integer serverPort = null;

    switch (protocol) {
      case POP3:
        serverPort = pop3ServerPort;
        break;
      case IMAP:
        Assert.notImplemented();
        break;
      case SMTP:
        serverPort = smtpServerPort;
        break;
    }
    return serverPort;
  }

  void processMessage(String message, String recipient) {
    mail.storeProxyMail(message, recipient);
  }

  @PreDestroy
  private void destroy() {
    tearDownServer();
  }

  private boolean initParameters(Protocol protocol) {
    resetParameters(protocol);

    String server = prm.getText(protocol + "Server");
    Number serverPort = prm.getNumber(protocol + "ServerPort");
    Number bindPort = prm.getNumber(protocol + "BindPort");

    boolean ok = !BeeUtils.isEmpty(server) && BeeUtils.allNotNull(serverPort, bindPort);

    switch (protocol) {
      case POP3:
        if (ok) {
          pop3ServerName = server;
          pop3ServerPort = serverPort.intValue();
          pop3BindPort = bindPort.intValue();
        }
        break;

      case IMAP:
        Assert.notImplemented();
        break;

      case SMTP:
        if (ok) {
          smtpServerName = server;
          smtpServerPort = serverPort.intValue();
          smtpBindPort = bindPort.intValue();
        }
        break;
    }
    return ok;
  }

  private void resetParameters(Protocol protocol) {
    switch (protocol) {
      case POP3:
        pop3ServerName = null;
        pop3ServerPort = 0;
        pop3BindPort = 0;
        pop3Proxy = null;
        break;

      case IMAP:
        Assert.notImplemented();
        break;

      case SMTP:
        smtpServerName = null;
        smtpServerPort = 0;
        smtpBindPort = 0;
        smtpProxy = null;
        break;
    }
  }

  private void tearDownServer() {
    if (pop3Proxy != null) {
      pop3Proxy.tearDown();
      resetParameters(Protocol.POP3);
      logger.info("POP3 proxy closed");
    }
    if (smtpProxy != null) {
      smtpProxy.tearDown();
      resetParameters(Protocol.SMTP);
      logger.info("SMTP proxy closed");
    }
  }
}
