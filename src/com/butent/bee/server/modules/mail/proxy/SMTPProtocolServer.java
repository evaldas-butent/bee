package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.modules.mail.MailConstants.Protocol;

public class SMTPProtocolServer extends TextBasedProtocolServer {

  public SMTPProtocolServer(MailProxy proxy) {
    super(proxy);
  }

  @Override
  protected int getBindPort() {
    return proxy.getBindPort(Protocol.SMTP);
  }

  @Override
  protected TextBasedProtocolServerHandler getHandlerInstance() {
    return new SMTPProtocolServerHandler(proxy);
  }
}
