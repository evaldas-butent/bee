package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public abstract class TextBasedProtocolServerHandler extends SimpleChannelHandler {
  /**
   * Closes the specified channel after all queued write requests are flushed.
   */
  static void closeOnFlush(Channel ch) {
    if (ch.isConnected()) {
      ch.write("").addListener(ChannelFutureListener.CLOSE);
    }
  }

  protected MailProxy proxy;

  final BeeLogger logger = LogUtils.getLogger(getClass());

  // This lock guards against the race condition that overrides the
  // OP_READ flag incorrectly.
  // See the related discussion: http://markmail.org/message/x7jc6mqx6ripynqf
  final Object trafficLock = new Object();

  private volatile Channel outboundChannel;

  public TextBasedProtocolServerHandler(MailProxy proxy) {
    this.proxy = proxy;
  }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
    if (outboundChannel != null) {
      try {
        closeOnFlush(outboundChannel);
      } catch (Exception ex) {
      }
    }
    logger.debug("Client at address", e.getChannel().getRemoteAddress(), "disconnected");
  }

  @Override
  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) {
    if (outboundChannel != null) {
      // If inboundChannel is not saturated anymore, continue accepting
      // the incoming traffic from the outboundChannel.
      synchronized (trafficLock) {
        if (e.getChannel().isWritable()) {
          outboundChannel.setReadable(true);
        }
      }
    }
  }

  @Override
  public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
    final Channel inboundChannel = e.getChannel();
    inboundChannel.setReadable(false);
    TextBasedProtocolClient cli = getClientInstance(inboundChannel, trafficLock);
    outboundChannel = cli.getFuture().getChannel();
    logger.debug("Client connected from", inboundChannel.getRemoteAddress());

    cli.getFuture().addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) {
        if (future.isSuccess()) {
          inboundChannel.setReadable(true);
        } else {
          inboundChannel.close();
        }
      }
    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    logger.debug("Logged server exception", e.getCause());
  }

  public abstract TextBasedProtocolClient getClientInstance(Channel inboundChannel, Object tl);

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    String msg = (String) e.getMessage();

    synchronized (trafficLock) {
      outboundChannel.write(msg + "\r\n");
      if (!outboundChannel.isWritable()) {
        e.getChannel().setReadable(false);
      }
    }
  }
}
