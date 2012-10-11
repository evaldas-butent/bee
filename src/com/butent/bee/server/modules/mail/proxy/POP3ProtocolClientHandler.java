package com.butent.bee.server.modules.mail.proxy;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class POP3ProtocolClientHandler extends TextBasedProtocolClientHandler {

  private String user = "";
  private String mailBody = "";
  private int state = 0;

  public POP3ProtocolClientHandler(Channel inboundChannel, Object tl, MailProxy proxy) {
    super(inboundChannel, tl, proxy);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    String msg = (String) e.getMessage();

    logger.debug("POP3 MR:", msg);

    if (state == 1) {
      if (msg.equals(".")) {
        proxy.processMessage(mailBody, user);
        state = 0;
        mailBody = "";

      } else if (msg.startsWith("-ERR")) {
        state = 0;
        mailBody = "";
        logger.severe("POP3 mail receive error:", msg);

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

    if (msg.toUpperCase().startsWith("USER")) {
      user = msg.substring(4).trim();
    } else if (msg.toUpperCase().startsWith("RETR")) {
      state = 1;
      mailBody = "";
    }
    super.writeRequested(ctx, e);
  }
}
