package com.butent.bee.server.modules.mail.proxy;

import org.jboss.netty.channel.Channel;

public class POP3ProtocolServerHandler extends TextBasedProtocolServerHandler {

  public POP3ProtocolServerHandler(MailProxy proxy) {
    super(proxy);
  }

  @Override
  public TextBasedProtocolClient getClientInstance(Channel inboundChannel, Object tl) {
    return new POP3ProtocolClient(inboundChannel, tl, getProxy());
  }
}
