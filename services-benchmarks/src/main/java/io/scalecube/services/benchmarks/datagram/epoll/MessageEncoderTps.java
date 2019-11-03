package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.scalecube.services.benchmarks.datagram.Configurations;
import java.util.List;

class MessageEncoderTps extends MessageToMessageEncoder<Integer> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Integer i, List<Object> out) {
    out.add(Configurations.PAYLOAD.retainedSlice());
  }
}
