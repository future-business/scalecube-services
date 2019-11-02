package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.scalecube.services.benchmarks.datagram.Configurations;
import io.scalecube.services.benchmarks.datagram.RateReporter;

public class EpollDatagramReceiverTps {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(EpollDatagramReceiverTps.class);

    RateReporter reporter = new RateReporter();

    Bootstrap bootstrap =
        new Bootstrap()
            .localAddress(9000)
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.SO_REUSEADDR, true)
            .channel(EpollDatagramChannel.class)
            .group(new EpollEventLoopGroup(1))
            .handler(
                new ChannelInitializer<Channel>() {
                  @Override
                  protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(new ChannelHandlerImpl());
                    ch.pipeline().addLast(new ByteToMessageDecoderImpl(reporter));
                  }
                });

    Channel channel = bootstrap.bind().channel();

    channel.closeFuture().get();
  }
}
