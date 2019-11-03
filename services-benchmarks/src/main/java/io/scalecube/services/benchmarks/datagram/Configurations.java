package io.scalecube.services.benchmarks.datagram;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.HdrHistogram.Recorder;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class Configurations {

  public static final int REPORT_INTERVAL = Integer.getInteger("reporter.interval", 1);
  public static final int WARMUP_REPORT_DELAY =
      Integer.getInteger("reporter.delay", REPORT_INTERVAL);
  public static final int MESSAGE_LENGTH = Integer.getInteger("messageLength", 65535 - 20 - 8);
  public static final int RCV_BUFFER_SIZE = Integer.getInteger("rcvBufferSize", 212992);
  public static final int SND_BUFFER_SIZE = Integer.getInteger("sndBufferSize", 212992);
  public static final InetSocketAddress PING_ADDRESS;
  public static final InetSocketAddress PONG_ADDRESS;
  public static final ByteBuffer SENDER_BUFFER;
  public static final ByteBuffer RECEIVER_BUFFER;
  public static final ByteBuf PAYLOAD =
      ByteBufAllocator.DEFAULT.buffer(Configurations.MESSAGE_LENGTH);

  static {
    byte[] bytes = new byte[MESSAGE_LENGTH];
    new Random().nextBytes(bytes);
    SENDER_BUFFER = ByteBuffer.allocateDirect(MESSAGE_LENGTH);
    SENDER_BUFFER.put(bytes).position(0);
    RECEIVER_BUFFER = ByteBuffer.allocateDirect(MESSAGE_LENGTH);
    PING_ADDRESS = new InetSocketAddress(System.getProperty("pingAddress", "localhost"), 8000);
    PONG_ADDRESS = new InetSocketAddress(System.getProperty("pongAddress", "localhost"), 9000);
    PAYLOAD.writeBytes(bytes);
  }

  public static final Recorder HISTOGRAM = new Recorder(TimeUnit.SECONDS.toNanos(10), 3);

  private Configurations() {}

  public static void printSettings(Class<?> clazz) {
    System.out.printf(
        "### %s: ping address: %s, "
            + "pong address: %s, "
            + "SENDER_BUFFER capacity: %s, "
            + "RECEIVER_BUFFER capacity: %s, "
            + "socket send-buffer size: %s, "
            + "socket receive-buffer size: %s, "
            + "reporter interval: %ssec\n",
        clazz.getSimpleName(),
        PING_ADDRESS,
        PONG_ADDRESS,
        SENDER_BUFFER.capacity(),
        RECEIVER_BUFFER.capacity(),
        RCV_BUFFER_SIZE,
        SND_BUFFER_SIZE,
        REPORT_INTERVAL);
  }

  public static Disposable startReport() {
    return Flux.interval(
            Duration.ofSeconds(WARMUP_REPORT_DELAY), Duration.ofSeconds(REPORT_INTERVAL))
        .doOnNext(Configurations::report)
        .doFinally(Configurations::report)
        .subscribe();
  }

  public static void report(Object ignored) {
    System.out.println("---- PING/PONG HISTO ----");
    HISTOGRAM.getIntervalHistogram().outputPercentileDistribution(System.out, 5, 1000.0, false);
    System.out.println("---- PING/PONG HISTO ----");
  }
}
