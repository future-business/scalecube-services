package io.scalecube.services.benchmarks.datagram;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;

public class RawDatagramSenderTps {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(RawDatagramSenderTps.class);

    InetSocketAddress senderAddress = new InetSocketAddress(Configurations.RECEIVER_ADDRESS, 8000);

    DatagramChannel sender = DatagramChannel.open();
    sender.configureBlocking(false);
    sender.connect(senderAddress);
    do {
      TimeUnit.SECONDS.sleep(1);
    } while (!sender.isConnected());
    System.out.println("RawDatagramSenderTps.sender connected: " + senderAddress);

    // sender
    Thread senderThread =
        new Thread(
            () -> {
              while (true) {
                ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
                sndBuffer.putLong(0, System.nanoTime()); // put client time
                Configurations.write(sender, sndBuffer);
              }
            });
    senderThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    senderThread.start();
  }
}
