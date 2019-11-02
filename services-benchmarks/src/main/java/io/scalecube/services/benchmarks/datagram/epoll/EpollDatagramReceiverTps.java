package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.scalecube.services.benchmarks.datagram.Configurations;
import io.scalecube.services.benchmarks.datagram.RateReporter;
import java.net.InetSocketAddress;

public class EpollDatagramReceiverTps {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(EpollDatagramReceiverTps.class);

    RateReporter reporter = new RateReporter();

    Bootstrap bootstrap =
        new Bootstrap()
            .channel(EpollDatagramChannel.class)
            .group(new EpollEventLoopGroup(1))
            .handler(
                new ChannelInitializer<Channel>() {
                  @Override
                  protected void initChannel(Channel ch) {
                    ch.pipeline().addLast(new ChannelHandlerImpl());
                    ch.pipeline()
                        .addLast(new DatagramPacketDecoder(new MessageDecoder(null, reporter)));
                  }
                });

    Channel channel = bootstrap.bind(new InetSocketAddress(9000)).await().channel();

    System.out.println("Channel pipeline: " + channel.pipeline());

    channel.closeFuture().get();
  }
}
