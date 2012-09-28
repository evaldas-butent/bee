package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.modules.mail.MailConstants.Protocol;

import org.jboss.netty.channel.Channel;

public class SMTPProtocolClient extends TextBasedProtocolClient {

  public SMTPProtocolClient(Channel inboundChannel, Object tl, MailProxy proxy) {
    super(inboundChannel, tl, proxy);
  }

  @Override
  public TextBasedProtocolClientHandler getClientHandlerInstance(Channel inboundChannel,
      Object tl) {
    return new SMTPProtocolClientHandler(inboundChannel, tl, proxy);
  }

  @Override
  protected String getHost() {
    return proxy.getServerName(Protocol.SMTP);
  }

  @Override
  protected int getPort() {
    return proxy.getServerPort(Protocol.SMTP);
  }
}
