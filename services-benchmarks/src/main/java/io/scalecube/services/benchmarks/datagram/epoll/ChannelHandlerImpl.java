package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ChannelHandlerImpl extends ChannelDuplexHandler {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channelActive: " + ctx.channel());
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channelInactive: " + ctx.channel());
    super.channelInactive(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // no-op
  }
}
