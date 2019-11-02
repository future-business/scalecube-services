package io.scalecube.services.benchmarks.datagram;

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

    InetSocketAddress receiverAddress = new InetSocketAddress(5678);
    InetSocketAddress senderAddress = new InetSocketAddress(Configurations.RECEIVER_ADDRESS, 8000);

    DatagramChannel receiver = DatagramChannel.open();
    DatagramSocket socket = receiver.socket();
    socket.setReuseAddress(true);
    socket.bind(receiverAddress);
    receiver.configureBlocking(false);
    System.out.println("RawDatagramPing.receiver bound: " + receiver + " on " + receiverAddress);

    DatagramChannel sender = DatagramChannel.open();
    sender.configureBlocking(false);
    sender.connect(senderAddress);
    do {
      TimeUnit.SECONDS.sleep(1);
    } while (!sender.isConnected());
    System.out.println("RawDatagramPing.sender connected: " + senderAddress);

    Configurations.HISTOGRAM.reset();
    Disposable reporter = Configurations.startReport();

    // sender
    Thread senderThread =
        new Thread(
            () -> {
              while (true) {
                ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
                sndBuffer.putLong(0, System.nanoTime()); // put client time
                Configurations.write(sender, sndBuffer);
                ByteBuffer rcvBuffer = (ByteBuffer) Configurations.RECEIVER_BUFFER.position(0);
                SocketAddress srcAddress = Configurations.receive(receiver, rcvBuffer);
                if (srcAddress != null) {
                  long rcvTime = System.nanoTime();
                  long sndTime = rcvBuffer.getLong(0);
                  long srvTime = rcvBuffer.getLong(8);
                  Configurations.HISTOGRAM.recordValue(rcvTime - sndTime);
                }
              }
            });
    senderThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    senderThread.start();
  }
}
