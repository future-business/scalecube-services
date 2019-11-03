package io.scalecube.services.benchmarks.datagram.raw;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import reactor.core.Exceptions;

class Runners {

  public static final int SANITY_NUMBER = 42100500;

  private Runners() {}

  static SocketAddress receive(DatagramChannel receiver, ByteBuffer rcvBuffer) {
    if (rcvBuffer.position() != 0) {
      throw new IllegalArgumentException(
          "rcvBuffer.position=" + rcvBuffer.position() + ", expected 0");
    }

    SocketAddress srcAddress = null;
    try {
      srcAddress = receiver.receive(rcvBuffer);
    } catch (PortUnreachableException e) {
      // no-op
    } catch (IOException e) {
      throw Exceptions.propagate(e);
    }

    if (srcAddress != null) {
      if (rcvBuffer.position() != rcvBuffer.capacity()) {
        throw new RuntimeException(
            "rcvBuffer.position=" + rcvBuffer.position() + ", expected " + rcvBuffer.capacity());
      }
      if (rcvBuffer.getInt(rcvBuffer.capacity() - 4) != SANITY_NUMBER) {
        throw new IllegalArgumentException("rcvBuffer isn't prepended with SANITY_NUMBER");
      }
    }

    return srcAddress;
  }

  static void write(DatagramChannel sender, ByteBuffer sndBuffer) {
    if (sndBuffer.position() != 0) {
      throw new IllegalArgumentException(
          "sndBuffer.position=" + sndBuffer.position() + ", expected 0");
    }

    if (!sender.isConnected()) {
      return;
    }

    sndBuffer.putInt(sndBuffer.capacity() - 4, SANITY_NUMBER);

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
