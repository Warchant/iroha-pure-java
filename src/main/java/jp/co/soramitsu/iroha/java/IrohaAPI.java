package jp.co.soramitsu.iroha.java;

import static jp.co.soramitsu.iroha.java.Utils.createTxList;
import static jp.co.soramitsu.iroha.java.Utils.createTxStatusRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.Observable;
import iroha.protocol.CommandServiceGrpc;
import iroha.protocol.CommandServiceGrpc.CommandServiceBlockingStub;
import iroha.protocol.CommandServiceGrpc.CommandServiceStub;
import iroha.protocol.Endpoint.ToriiResponse;
import iroha.protocol.QryResponses.BlockQueryResponse;
import iroha.protocol.QryResponses.QueryResponse;
import iroha.protocol.Queries;
import iroha.protocol.QueryServiceGrpc;
import iroha.protocol.QueryServiceGrpc.QueryServiceBlockingStub;
import iroha.protocol.QueryServiceGrpc.QueryServiceStub;
import iroha.protocol.TransactionOuterClass;
import java.io.Closeable;
import java.net.URI;
import jp.co.soramitsu.iroha.java.detail.StreamObserverToEmitter;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Class which provides convenient RX abstraction over Iroha API.
 */
@Getter
public class IrohaAPI implements Closeable {

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


  /**
   * Send transaction asynchronously.
   *
   * @param tx protobuf transaction.
   * @return observable. Use {@code Observable.blockingSubscribe(...)} or {@code
   * Observable.subscribe} for synchronous or asynchronous subscription.
   */
  public Observable<ToriiResponse> transaction(TransactionOuterClass.Transaction tx) {
    transactionSync(tx);
    byte[] hash = Utils.hash(tx);
    return txStatus(hash);
  }

  /**
   * Send transaction synchronously.
   *
   * Blocking call.
   *
   * @param tx protobuf transaction.
   */
  public void transactionSync(TransactionOuterClass.Transaction tx) {
    cmdStub.torii(tx);
  }

  /**
   * Send query synchronously.
   *
   * @param query protobuf query.
   */
  public QueryResponse query(Queries.Query query) {
    return queryStub.find(query);
  }

  /**
   * Subscribe for blocks in iroha. You need to have special permission to do that.
   *
   * @param query protobuf query.
   */
  public Observable<BlockQueryResponse> blocksQuery(Queries.BlocksQuery query) {
    return Observable.create(
        o -> queryStreamingStub.fetchCommits(query, new StreamObserverToEmitter<>(o))
    );
  }

  /**
   * Synchronously send list of transactions.
   *
   * Blocking call.
   */
  public void transactionListSync(Iterable<TransactionOuterClass.Transaction> txList) {
    cmdStub.listTorii(createTxList(txList));
  }

  /**
   * Asynchronously ask for transaction status.
   *
   * @param txHash hash of transaction for status query.
   * @return {@link Observable}
   */
  public Observable<ToriiResponse> txStatus(byte[] txHash) {
    val req = createTxStatusRequest(txHash);
    return Observable.create(
        o -> cmdStreamingStub.statusStream(req, new StreamObserverToEmitter<>(o))
    );
  }

  /**
   * Synchronously ask to transaction status.
   *
   * @param txHash hash of transaction for status query
   * @return {@link ToriiResponse} status code, error message (if any).
   */
  public ToriiResponse txStatusSync(byte[] txHash) {
    return cmdStub.status(createTxStatusRequest(txHash));
  }

  /**
   * Close GRPC connection.
   */
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
