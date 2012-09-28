package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public abstract class TextBasedProtocolClient {

  final BeeLogger logger = LogUtils.getLogger(getClass());

  protected MailProxy proxy;

  private ChannelFuture future;
  private Channel serverInChannel;
  private Object trafficlock;

  public TextBasedProtocolClient(Channel inboundChannel, Object tl, MailProxy proxy) {
    this.proxy = proxy;

    try {
      String host = getHost();
      int port = getPort();
      serverInChannel = inboundChannel;
      trafficlock = tl;

      ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
          Executors.newCachedThreadPool());
      ClientBootstrap bootstrap = new ClientBootstrap(factory);

      bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline pipeline = Channels.pipeline();
          pipeline.addLast("framer",
              new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
          pipeline.addLast("decoder", new StringDecoder());
          pipeline.addLast("encoder", new StringEncoder());

          pipeline.addLast("handler", getClientHandlerInstance(serverInChannel, trafficlock));
          return pipeline;
        }
      });

      bootstrap.setOption("tcpNoDelay", true);
      bootstrap.setOption("keepAlive", true);
      future = bootstrap.connect(new InetSocketAddress(host, port));

    } catch (Exception e) {
      logger.error("ERROR: " + e.getCause().getMessage());
    }
  }

  public abstract TextBasedProtocolClientHandler getClientHandlerInstance(Channel inboundChannel,
      Object tl);

  public ChannelFuture getFuture() {
    return future;
  }

  protected abstract String getHost();

  protected abstract int getPort();
}
