package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.scalecube.services.benchmarks.datagram.RateReporter;
import java.util.List;
import org.HdrHistogram.Recorder;

class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

  private final Recorder histogram;
  private final RateReporter reporter;

  MessageDecoder(Recorder histogram, RateReporter reporter) {
    this.histogram = histogram;
    this.reporter = reporter;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
    if (histogram != null) {
      long start = msg.getLong(0);
      histogram.recordValue(System.nanoTime() - start);
    }
    if (reporter != null) {
      reporter.onMessage(1, msg.readableBytes());
    }
  }
}
