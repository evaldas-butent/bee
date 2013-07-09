package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.modules.mail.MailConstants.Protocol;

import org.jboss.netty.channel.Channel;

public class POP3ProtocolClient extends TextBasedProtocolClient {

  public POP3ProtocolClient(Channel inboundChannel, Object tl, MailProxy proxy) {
    super(inboundChannel, tl, proxy);
  }

  @Override
  public TextBasedProtocolClientHandler getClientHandlerInstance(Channel inboundChannel,
      Object tl) {
    return new POP3ProtocolClientHandler(inboundChannel, tl, getProxy());
  }

  @Override
  protected String getHost() {
    return getProxy().getServerName(Protocol.POP3);
  }

  @Override
  protected int getPort() {
    return getProxy().getServerPort(Protocol.POP3);
  }
}
