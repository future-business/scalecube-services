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
            .channel(EpollDatagramChannel.class)
            .group(new EpollEventLoopGroup(1))
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.SO_RCVBUF, Configurations.SOCKET_BUFFER_SIZE)
            .option(ChannelOption.SO_SNDBUF, Configurations.SOCKET_BUFFER_SIZE)
            .handler(
                new ChannelInitializer<Channel>() {
                  @Override
                  protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(new ChannelHandlerImpl());
                    ch.pipeline().addLast(new ChannelWriter());
                    ch.pipeline().addLast(new MessageEncoderTps());
                  }
                });

    Channel channel = bootstrap.connect().await().channel();

    System.out.println("Channel pipeline: " + channel.pipeline());

    channel.pipeline().fireUserEventTriggered("ChannelWriter.start");

    channel.closeFuture().get();
  }
}
