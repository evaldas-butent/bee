package com.butent.bee.server.modules.mail.proxy;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public abstract class TextBasedProtocolServer {
  final BeeLogger logger = LogUtils.getLogger(getClass());

  protected MailProxy proxy;

  private Channel chan;
  private ServerBootstrap bs;

  public TextBasedProtocolServer(MailProxy proxy) {
    this.proxy = proxy;
    ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool());
    ServerBootstrap serverBootstrap = new ServerBootstrap(factory);
    bs = serverBootstrap;

    serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("framer",
            new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        pipeline.addLast("handler", getHandlerInstance());

        return pipeline;
      }
    });

    try {
      chan = serverBootstrap.bind(new InetSocketAddress(getBindPort()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void teardown() {
    chan.close().awaitUninterruptibly();
    bs.releaseExternalResources();
  }

  protected abstract int getBindPort();

  protected abstract TextBasedProtocolServerHandler getHandlerInstance();
}
