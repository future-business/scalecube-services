package io.scalecube.services.benchmarks.datagram.raw;

import io.scalecube.services.benchmarks.datagram.Configurations;
import io.scalecube.services.benchmarks.datagram.RateReporter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class RawDatagramReceiverTps {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(RawDatagramReceiverTps.class);

    InetSocketAddress receiverAddress = new InetSocketAddress(9000);

    DatagramChannel receiver = DatagramChannel.open();
    receiver.socket().setReuseAddress(true);
    receiver.socket().setReceiveBufferSize(Configurations.RCV_BUFFER_SIZE);
    receiver.socket().setSendBufferSize(Configurations.SND_BUFFER_SIZE);
    receiver.socket().bind(receiverAddress);
    receiver.configureBlocking(false);
    System.out.println(
        "RawDatagramReceiverTps.receiver bound: " + receiver + " on " + receiverAddress);

    RateReporter reporter = new RateReporter();

    // receiver
    System.out.println("Receiving..");
    while (true) {
      ByteBuffer rcvBuffer = (ByteBuffer) Configurations.RECEIVER_BUFFER.position(0);
      SocketAddress srcAddress = Runners.receive(receiver, rcvBuffer);
      if (srcAddress != null) {
        reporter.onMessage(1, rcvBuffer.capacity());
      }
    }
  }
}
