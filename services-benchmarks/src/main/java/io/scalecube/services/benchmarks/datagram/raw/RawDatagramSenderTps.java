package io.scalecube.services.benchmarks.datagram.raw;

import io.scalecube.services.benchmarks.datagram.Configurations;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;

public class RawDatagramSenderTps {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(RawDatagramSenderTps.class);

    DatagramChannel sender = DatagramChannel.open();
    sender.configureBlocking(false);
    sender.connect(Configurations.PONG_ADDRESS);
    do {
      TimeUnit.SECONDS.sleep(1);
    } while (!sender.isConnected());
    System.out.println("RawDatagramSenderTps.sender connected: " + Configurations.PONG_ADDRESS);

    // sender
    Thread senderThread =
        new Thread(
            () -> {
              System.out.println("Sending..");
              while (true) {
                ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
                Runners.write(sender, sndBuffer);
              }
            });
    senderThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    senderThread.start();
  }
}
