package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.scalecube.services.benchmarks.datagram.Configurations;
import java.util.List;

class MessageEncoder extends MessageToMessageEncoder<Integer> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Integer i, List<Object> out) {
    ByteBuf byteBuf = ctx.alloc().buffer(Configurations.MESSAGE_LENGTH);
    byteBuf.writeBytes(Configurations.PAYLOAD.slice());
    out.add(byteBuf);
  }
}
