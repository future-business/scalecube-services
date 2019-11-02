package io.scalecube.services.benchmarks.datagram.raw;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import reactor.core.Exceptions;

class Runners {

  private Runners() {}

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
