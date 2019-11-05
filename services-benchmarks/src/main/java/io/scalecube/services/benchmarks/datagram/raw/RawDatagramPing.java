package io.scalecube.services.benchmarks.datagram.raw;

import io.scalecube.services.benchmarks.datagram.Configurations;
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
    receiver.socket().setReuseAddress(true);
    receiver.socket().setReceiveBufferSize(Configurations.RCV_BUFFER_SIZE);
    receiver.socket().setSendBufferSize(Configurations.SND_BUFFER_SIZE);
    receiver.socket().bind(receiverAddress);
    receiver.configureBlocking(false);
    System.out.println("RawDatagramPing.receiver bound: " + receiver + " on " + receiverAddress);

    DatagramChannel sender = DatagramChannel.open();
    sender.socket().setReuseAddress(true);
    sender.socket().setReceiveBufferSize(Configurations.RCV_BUFFER_SIZE);
    sender.socket().setSendBufferSize(Configurations.SND_BUFFER_SIZE);
    sender.configureBlocking(false);
    sender.connect(Configurations.PONG_ADDRESS);
    do {
      TimeUnit.SECONDS.sleep(1);
    } while (!sender.isConnected());
    System.out.println("RawDatagramPing.sender connected: " + Configurations.PONG_ADDRESS);

    Configurations.HISTOGRAM.reset();
    Disposable reporter = Configurations.startReport();

    // sender and receiver
    System.out.println("Sending and Receiving..");
    while (true) {
      ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
      sndBuffer.putLong(0, System.nanoTime()); // put start time
      Runners.write(sender, sndBuffer);

      ByteBuffer rcvBuffer = (ByteBuffer) Configurations.RECEIVER_BUFFER.position(0);
      SocketAddress srcAddress = Runners.receive(receiver, rcvBuffer);
      if (srcAddress != null) {
        long start = rcvBuffer.getLong(0);
        Configurations.HISTOGRAM.recordValue(System.nanoTime() - start);
      }
    }
  }
}
