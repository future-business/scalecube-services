package io.scalecube.services.benchmarks.datagram.raw;

import io.scalecube.services.benchmarks.datagram.Configurations;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;

public class RawDatagramPong {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(RawDatagramPong.class);

    InetSocketAddress receiverAddress = new InetSocketAddress(9000);

    DatagramChannel receiver = DatagramChannel.open();
    receiver.socket().setReuseAddress(true);
    receiver.socket().setReceiveBufferSize(Configurations.RCV_BUFFER_SIZE);
    receiver.socket().setSendBufferSize(Configurations.SND_BUFFER_SIZE);
    receiver.socket().bind(receiverAddress);
    receiver.configureBlocking(false);
    System.out.println("RawDatagramPong.receiver bound: " + receiver + " on " + receiverAddress);

    DatagramChannel sender = DatagramChannel.open();
    sender.socket().setReuseAddress(true);
    sender.socket().setReceiveBufferSize(Configurations.RCV_BUFFER_SIZE);
    sender.socket().setSendBufferSize(Configurations.SND_BUFFER_SIZE);
    sender.configureBlocking(false);
    sender.connect(Configurations.PING_ADDRESS);
    do {
      TimeUnit.SECONDS.sleep(1);
    } while (!sender.isConnected());
    System.out.println("RawDatagramPong.sender connected: " + Configurations.PING_ADDRESS);

    // receiver
    System.out.println("Receiving..");
    while (true) {
      ByteBuffer rcvBuffer = (ByteBuffer) Configurations.RECEIVER_BUFFER.position(0);
      SocketAddress srcAddress = Runners.receive(receiver, rcvBuffer);
      if (srcAddress != null) {
        ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
        sndBuffer.putLong(0, rcvBuffer.getLong(0)); // copy start time
        Runners.write(sender, sndBuffer);
      }
    }
  }
}
