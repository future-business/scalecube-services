package io.scalecube.services.benchmarks.datagram.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.scalecube.services.benchmarks.datagram.RateReporter;
import java.util.List;
import org.HdrHistogram.Recorder;

public class ByteToMessageDecoderImpl extends ByteToMessageDecoder {

  private Recorder histogram;
  private RateReporter reporter;

  public ByteToMessageDecoderImpl(Recorder histogram) {
    this.histogram = histogram;
  }

  public ByteToMessageDecoderImpl(RateReporter reporter) {
    this.reporter = reporter;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    if (histogram != null) {
      long start = in.getLong(0);
      histogram.recordValue(System.nanoTime() - start);
    }
    if (reporter != null) {
      reporter.onMessage(1, in.readableBytes());
    }
    in.release();
  }
}
