package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class SenderChannelHandlerImpl extends ChannelDuplexHandler {

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof String && evt.equals("doSend")) {
      Channel channel = ctx.channel();
      System.out.println("Sending, " + ctx + ", " + channel);
      while (true) {
        if (channel.isActive()) {
          channel.writeAndFlush(1, ctx.voidPromise());
        }
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }
}
