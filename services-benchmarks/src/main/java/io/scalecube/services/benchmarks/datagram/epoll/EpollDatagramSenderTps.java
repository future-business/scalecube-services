package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.scalecube.services.benchmarks.datagram.Configurations;

public class EpollDatagramSenderTps {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(EpollDatagramSenderTps.class);

    Bootstrap bootstrap =
        new Bootstrap()
            .remoteAddress(Configurations.PONG_ADDRESS)
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.SO_REUSEADDR, true)
            .channel(EpollDatagramChannel.class)
            .group(new EpollEventLoopGroup(1))
            .handler(
                new ChannelInitializer<Channel>() {
                  @Override
                  protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(new ChannelHandlerImpl());
                    ch.pipeline().addLast(new SenderChannelHandlerImpl());
                    ch.pipeline().addLast(new MessageToByteEncoderImpl());
                  }
                });

    Channel channel = bootstrap.connect().await().channel();
    channel.pipeline().fireUserEventTriggered("doSend");

    channel.closeFuture().get();
  }
}
