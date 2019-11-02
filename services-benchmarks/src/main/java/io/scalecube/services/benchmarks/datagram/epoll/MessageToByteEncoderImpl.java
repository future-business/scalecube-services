package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.scalecube.services.benchmarks.datagram.Configurations;

class MessageToByteEncoderImpl extends MessageToByteEncoder<Integer> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Integer msg, ByteBuf out) {
    out.writeLong(System.nanoTime());
    out.writeBytes(Configurations.PAYLOAD.slice());
  }
}
