package io.scalecube.services.examples;

import io.netty.buffer.ByteBuf;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.ByteBufPayload;
import java.util.Objects;
import java.util.stream.LongStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Example {
  static {
    ResourceLeakDetector.setLevel(Level.PARANOID);
    System.setProperty("logging.level.reactor.ipc.netty", "DEBUG");
  }

  public static void main(String[] args) {
    CloseableChannel server = null;
    RSocket client = null;
    try {
      for (int j = 0; j < 1000; j++) {
        // System.out.println("iteration " + j);
        server =
            Objects.requireNonNull(
                RSocketFactory.receive()
                    .frameDecoder(PayloadDecoder.ZERO_COPY)
                    .errorConsumer(th -> System.err.println("[Server]: " + th.getMessage()))
                    .acceptor(new SocketAcceptorImpl())
                    .transport(TcpServerTransport.create("localhost", 7000))
                    .start()
                    .doOnError(th -> System.err.println("[Server bind]: " + th.getMessage()))
                    .block());
        client =
            Objects.requireNonNull(
                RSocketFactory.connect()
                    .frameDecoder(PayloadDecoder.ZERO_COPY)
                    .errorConsumer(th -> System.err.println("[Client]: " + th.getMessage()))
                    .transport(TcpClientTransport.create("localhost", 7000))
                    .start()
                    .doOnError(th -> System.err.println("[Client connect]: " + th.getMessage()))
                    .block());
        for (int i = 0; i < 100; i++) {
          // System.out.println(i);
          client
              .requestStream(ByteBufPayload.create("data" + i, "metadata" + i))
              .take(1)
              .doOnNext(
                  p -> {
                    ByteBuf data = p.sliceData().retain();
                    ByteBuf metadata = p.sliceMetadata().retain();
                    p.release();
                    data.release();
                    metadata.release();
                  })
              .blockFirst();
        }
        client.dispose();
        server.dispose();
        client.onClose().block();
        server.onClose().block();
      }
    } catch (Throwable th) {
      th.printStackTrace();
      if (client != null) {
        client.dispose();
      }
      if (server != null) {
        server.dispose();
      }
    }
  }

  private static class SocketAcceptorImpl implements SocketAcceptor {
    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setupPayload, RSocket reactiveSocket) {
      return Mono.just(
          new AbstractRSocket() {
            @Override
            public Flux<Payload> requestStream(Payload payload) {
              // System.out.println(payload.getDataUtf8());
              payload.release();
              return Flux.defer(() -> Flux.fromStream(LongStream.range(0, 30).boxed()))
                  .map(i -> ByteBufPayload.create("data" + i, "metadata" + i));
            }
          });
    }
  }
}
