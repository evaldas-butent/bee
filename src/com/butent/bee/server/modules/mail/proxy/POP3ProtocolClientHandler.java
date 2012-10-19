package com.butent.bee.server.modules.mail.proxy;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class POP3ProtocolClientHandler extends TextBasedProtocolClientHandler {

  private String user = "";
  private StringBuilder mailBody = null;
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
        proxy.processMessage(mailBody.toString(), user);
        state = 0;
        mailBody = null;

      } else if (msg.startsWith("-ERR")) {
        state = 0;
        mailBody = null;
        logger.severe("POP3 mail receive error:", msg);

      } else if (!msg.startsWith("+OK")) {
        mailBody.append(msg.startsWith("..") ? msg.substring(1) : msg).append("\r\n");
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
      mailBody = new StringBuilder();
    }
    super.writeRequested(ctx, e);
  }
}
