package jp.co.soramitsu.iroha.java;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import iroha.protocol.CommandServiceGrpc;
import iroha.protocol.QueryServiceGrpc;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class IrohaEndpointBlocking {

  private CommandServiceGrpc.CommandServiceBlockingStub commandServiceGrpc;
  private QueryServiceGrpc.QueryServiceBlockingStub queryServiceGrpc;

  private ManagedChannel channel;

  private ManagedChannel createChannel(String host, int port) {
    return ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();
  }

  private void createBlocking(ManagedChannel channel) {
    this.channel = channel;

    this.commandServiceGrpc = CommandServiceGrpc.newBlockingStub(channel);
    this.queryServiceGrpc = QueryServiceGrpc.newBlockingStub(channel);
  }

  public IrohaEndpointBlocking(String host, int port) {
    createBlocking(
        createChannel(host, port)
    );
  }

  public IrohaEndpointBlocking(String address) {
    URI uri = URI.create(address);

    createBlocking(
        createChannel(uri.getHost(), uri.getPort())
    );
  }

  public IrohaEndpointBlocking(ManagedChannel channel) {
    createBlocking(channel);
  }


  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void sendTransaction(Transaction tx){
    commandServiceGrpc.torii(tx.build());
  }
}
