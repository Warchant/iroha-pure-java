package jp.co.soramitsu.iroha.java;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.Observable;
import iroha.protocol.CommandServiceGrpc;
import iroha.protocol.CommandServiceGrpc.CommandServiceBlockingStub;
import iroha.protocol.CommandServiceGrpc.CommandServiceStub;
import iroha.protocol.Endpoint.ToriiResponse;
import iroha.protocol.Endpoint.TxStatusRequest;
import iroha.protocol.QryResponses.BlockQueryResponse;
import iroha.protocol.QryResponses.QueryResponse;
import iroha.protocol.Queries;
import iroha.protocol.QueryServiceGrpc;
import iroha.protocol.QueryServiceGrpc.QueryServiceBlockingStub;
import iroha.protocol.QueryServiceGrpc.QueryServiceStub;
import iroha.protocol.TransactionOuterClass;
import java.io.Closeable;
import java.net.URI;
import jp.co.soramitsu.iroha.java.detail.StreamObserverToSubject;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class IrohaAPI implements AutoCloseable, Closeable {

  private URI uri;
  private ManagedChannel channel;
  private CommandServiceBlockingStub cmdStub;
  private CommandServiceStub cmdStreamingStub;
  private QueryServiceBlockingStub queryStub;
  private QueryServiceStub queryStreamingStub;

  public IrohaAPI(URI uri) {
    this(uri.getHost(), uri.getPort());
  }

  @SneakyThrows
  public IrohaAPI(String host, int port) {
    channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    cmdStub = CommandServiceGrpc.newBlockingStub(channel);
    queryStub = QueryServiceGrpc.newBlockingStub(channel);
    cmdStreamingStub = CommandServiceGrpc.newStub(channel);
    queryStreamingStub = QueryServiceGrpc.newStub(channel);

    this.uri = new URI("grpc", null, host, port, null, null, null);
  }


  public Observable<ToriiResponse> transaction(TransactionOuterClass.Transaction tx) {
    cmdStub.torii(tx);

    byte[] hash = Utils.hash(tx);
    TxStatusRequest req = TxStatusRequest.newBuilder()
        .setTxHash(ByteString.copyFrom(hash))
        .build();

    return Observable.create(
        o -> cmdStreamingStub.statusStream(req, new StreamObserverToSubject<>(o))
    );
  }

  public QueryResponse query(Queries.Query query) {
    return queryStub.find(query);
  }

  public Observable<BlockQueryResponse> blocksQuery(Queries.BlocksQuery query) {
    return Observable
        .create(o -> queryStreamingStub
            .fetchCommits(query, new StreamObserverToSubject<>(o))
        );
  }

  public void terminate() {
    if (!channel.isTerminated()) {
      channel.shutdownNow();
    }
  }

  @Override
  public void finalize() {
    terminate();
  }

  @Override
  public void close() {
    terminate();
  }
}
