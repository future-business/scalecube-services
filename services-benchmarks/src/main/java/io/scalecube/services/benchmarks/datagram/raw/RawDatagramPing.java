package io.scalecube.services.benchmarks.datagram.raw;

import io.scalecube.services.benchmarks.datagram.Configurations;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;
import reactor.core.Disposable;

public class RawDatagramPing {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(RawDatagramPing.class);

    InetSocketAddress receiverAddress = new InetSocketAddress(8000);

    DatagramChannel receiver = DatagramChannel.open();
    DatagramSocket socket = receiver.socket();
    socket.bind(receiverAddress);
    receiver.configureBlocking(false);
    System.out.println("RawDatagramPing.receiver bound: " + receiver + " on " + receiverAddress);

    DatagramChannel sender = DatagramChannel.open();
    sender.configureBlocking(false);
    sender.connect(Configurations.PONG_ADDRESS);
    do {
      TimeUnit.SECONDS.sleep(1);
    } while (!sender.isConnected());
    System.out.println("RawDatagramPing.sender connected: " + Configurations.PONG_ADDRESS);

    Configurations.HISTOGRAM.reset();
    Disposable reporter = Configurations.startReport();

    // sender
    Thread senderThread =
        new Thread(
            () -> {
              while (true) {
                ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
                sndBuffer.putLong(0, System.nanoTime()); // put start time
                Runners.write(sender, sndBuffer);
              }
            });
    senderThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    senderThread.start();

    // receiver
    Thread receiverThread =
        new Thread(
            () -> {
              while (true) {
                ByteBuffer rcvBuffer = (ByteBuffer) Configurations.RECEIVER_BUFFER.position(0);
                SocketAddress srcAddress = Runners.receive(receiver, rcvBuffer);
                if (srcAddress != null) {
                  long start = rcvBuffer.getLong(0);
                  Configurations.HISTOGRAM.recordValue(System.nanoTime() - start);
                }
              }
            });
    receiverThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    receiverThread.start();
  }
}
