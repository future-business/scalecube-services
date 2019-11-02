package io.scalecube.services.benchmarks.datagram;

import java.io.IOException;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.HdrHistogram.Recorder;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class Configurations {

  static final int REPORT_INTERVAL = Integer.getInteger("reporter.interval", 1);
  static final int WARMUP_REPORT_DELAY = Integer.getInteger("reporter.delay", REPORT_INTERVAL);
  static final int MESSAGE_SIZE = Integer.getInteger("messageSize", 256);
  static final InetAddress RECEIVER_ADDRESS;

  static final ByteBuffer SENDER_BUFFER;
  static final ByteBuffer RECEIVER_BUFFER;

  static {
    byte[] bytes = new byte[MESSAGE_SIZE];
    new Random().nextBytes(bytes);
    SENDER_BUFFER = ByteBuffer.allocateDirect(MESSAGE_SIZE);
    SENDER_BUFFER.put(bytes).position(0);
    RECEIVER_BUFFER = ByteBuffer.allocateDirect(MESSAGE_SIZE);
    try {
      RECEIVER_ADDRESS = InetAddress.getByName(System.getProperty("receiverAddress", "localhost"));
    } catch (UnknownHostException e) {
      throw Exceptions.propagate(e);
    }
  }

  static final Recorder HISTOGRAM = new Recorder(TimeUnit.SECONDS.toNanos(10), 3);

  private Configurations() {}

  static void printSettings(Class<?> clazz) {
    System.out.printf(
        "### %s: remote receiver: %s, message size: %s, repoter interval: %s sec\n",
        clazz.getSimpleName(), RECEIVER_ADDRESS, MESSAGE_SIZE, REPORT_INTERVAL);
  }

  static Disposable startReport() {
    return Flux.interval(
            Duration.ofSeconds(WARMUP_REPORT_DELAY), Duration.ofSeconds(REPORT_INTERVAL))
        .doOnNext(Configurations::report)
        .doFinally(Configurations::report)
        .subscribe();
  }

  static void report(Object ignored) {
    System.out.println("---- PING/PONG HISTO ----");
    HISTOGRAM.getIntervalHistogram().outputPercentileDistribution(System.out, 5, 1000.0, false);
    System.out.println("---- PING/PONG HISTO ----");
  }

  static SocketAddress receive(DatagramChannel receiver, ByteBuffer rcvBuffer) {
    SocketAddress srcAddress = null;
    try {
      srcAddress = receiver.receive(rcvBuffer);
    } catch (PortUnreachableException e) {
      // no-op
    } catch (IOException e) {
      throw Exceptions.propagate(e);
    }
    if (srcAddress != null && rcvBuffer.position() != rcvBuffer.capacity()) {
      throw new RuntimeException(
          "rcvBuffer.position=" + rcvBuffer.position() + ", expected " + rcvBuffer.capacity());
    }
    return srcAddress;
  }

  static void write(DatagramChannel sender, ByteBuffer sndBuffer) {
    if (!sender.isConnected()) {
      return;
    }
    int w = 0;
    try {
      w = sender.write(sndBuffer);
    } catch (PortUnreachableException e) {
      // no-op
    } catch (IOException e) {
      throw Exceptions.propagate(e);
    }
    if (w > 0 && sndBuffer.position() != sndBuffer.capacity()) {
      throw new RuntimeException(
          "sndBuffer.position=" + sndBuffer.position() + ", expected " + sndBuffer.capacity());
    }
  }
}
