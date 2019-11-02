package io.scalecube.services.benchmarks.datagram;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.TimeUnit;

public class RawDatagramPong {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(RawDatagramPong.class);

    InetSocketAddress receiverAddress = new InetSocketAddress(8000);
    InetSocketAddress senderAddress = new InetSocketAddress(Configurations.RECEIVER_ADDRESS, 5678);

    DatagramChannel receiver = DatagramChannel.open();
    DatagramSocket socket = receiver.socket();
    socket.setReuseAddress(true);
    socket.bind(receiverAddress);
    receiver.configureBlocking(false);
    System.out.println("RawDatagramPong.receiver bound: " + receiver + " on " + receiverAddress);

    DatagramChannel sender = DatagramChannel.open();
    sender.configureBlocking(false);
    sender.connect(senderAddress);
    do {
      TimeUnit.SECONDS.sleep(1);
    } while (!sender.isConnected());
    System.out.println("RawDatagramPong.sender connected: " + senderAddress);

    // receiver
    Thread receiverThread =
        new Thread(
            () -> {
              while (true) {
                ByteBuffer rcvBuffer = (ByteBuffer) Configurations.RECEIVER_BUFFER.position(0);
                SocketAddress srcAddress = Configurations.receive(receiver, rcvBuffer);
                if (srcAddress != null) {
                  ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
                  sndBuffer.putLong(0, rcvBuffer.getLong(0)); // copy client time
                  sndBuffer.putLong(8, System.nanoTime()); // put server time
                  Configurations.write(sender, sndBuffer);
                }
              }
            });
    receiverThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    receiverThread.start();
  }
}
