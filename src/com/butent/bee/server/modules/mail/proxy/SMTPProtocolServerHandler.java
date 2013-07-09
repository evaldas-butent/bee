package com.butent.bee.server.modules.mail.proxy;

import org.jboss.netty.channel.Channel;

public class SMTPProtocolServerHandler extends TextBasedProtocolServerHandler {

  public SMTPProtocolServerHandler(MailProxy proxy) {
    super(proxy);
  }

  @Override
  public TextBasedProtocolClient getClientInstance(Channel inboundChannel, Object tl) {
    return new SMTPProtocolClient(inboundChannel, tl, getProxy());
  }
}
