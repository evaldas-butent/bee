package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.modules.mail.MailConstants.Protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public class SMTPProtocolClientHandler extends TextBasedProtocolClientHandler {

  private String mailBody = "";
  private int state = 0;

  public SMTPProtocolClientHandler(Channel inboundChannel, Object tl, MailProxy proxy) {
    super(inboundChannel, tl, proxy);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    String msg = (String) e.getMessage();

    logger.debug("SMTP MR:", msg);

    if (state == 3) {
      if (msg.startsWith("250")) {
        state = 0;
        proxy.processMessage(mailBody, Protocol.SMTP);

      } else {
        state = 0;
        logger.error("SMTP mail send error: " + msg);
      }
    }
    super.messageReceived(ctx, e);
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    String msg = (String) e.getMessage();

    logger.debug("SMTP WR:", msg);

    if (state == 0 && msg.toUpperCase().startsWith("DATA")) {
      state = 1;
      mailBody = "";

    } else {
      if (state == 1 && msg.equals(".\r\n")) {
        state = 3;
      } else if (state == 1) {
        mailBody += msg.startsWith("..") ? msg.substring(1) : msg;
      }
    }
    super.writeRequested(ctx, e);
  }
}
