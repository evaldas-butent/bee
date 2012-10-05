package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.modules.mail.MailConstants.Protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class POP3ProtocolClientHandler extends TextBasedProtocolClientHandler {

  private String mailBody = "";
  private int state = 0;

  public POP3ProtocolClientHandler(Channel inboundChannel, Object tl, MailProxy proxy) {
    super(inboundChannel, tl, proxy);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    String msg = (String) e.getMessage();

    logger.debug("POP3 MR:", msg);

    if (state == 1 && msg.equals(".")) {
      state = 0;
      proxy.processMessage(mailBody, Protocol.POP3);

    } else if (state == 1) {
      if (msg.startsWith("-ERR")) {
        state = 0;
        mailBody = "";
        logger.error("POP3 mail receive error: " + msg);

      } else if (!msg.startsWith("+OK")) {
        mailBody += (msg.startsWith("..") ? msg.substring(1) : msg) + "\r\n";
      }
    }
    super.messageReceived(ctx, e);
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    String msg = (String) e.getMessage();

    logger.debug("POP3 WR:", msg);

    if (msg.toUpperCase().startsWith("RETR")) {
      state = 1;
      mailBody = "";
    }
    super.writeRequested(ctx, e);
  }
}
