package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.modules.mail.MailConstants.Protocol;

public class POP3ProtocolServer extends TextBasedProtocolServer {

  public POP3ProtocolServer(MailProxy proxy) {
    super(proxy);
  }

  @Override
  protected int getBindPort() {
    return proxy.getBindPort(Protocol.POP3);
  }

  @Override
  protected TextBasedProtocolServerHandler getHandlerInstance() {
    return new POP3ProtocolServerHandler(proxy);
  }
}
