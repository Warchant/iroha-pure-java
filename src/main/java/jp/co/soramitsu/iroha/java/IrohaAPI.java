package jp.co.soramitsu.iroha.java;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
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
import java.net.URI;
import jp.co.soramitsu.iroha.java.detail.StreamObserverToSubject;

public class IrohaAPI {

  private CommandServiceBlockingStub cmdStub;
  private CommandServiceStub cmdStreamingStub;
  private QueryServiceBlockingStub queryStub;
  private QueryServiceStub queryStreamingStub;

  public IrohaAPI(URI uri) {
    this(uri.getHost(), uri.getPort());
  }

  public IrohaAPI(String host, int port) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    cmdStub = CommandServiceGrpc.newBlockingStub(channel);
    queryStub = QueryServiceGrpc.newBlockingStub(channel);
    cmdStreamingStub = CommandServiceGrpc.newStub(channel);
    queryStreamingStub = QueryServiceGrpc.newStub(channel);
  }


  public Subject<ToriiResponse> transaction(TransactionOuterClass.Transaction tx) {
    PublishSubject<ToriiResponse> subject = PublishSubject.create();
    cmdStub.torii(tx);

    byte[] hash = Utils.hash(tx);
    TxStatusRequest req = TxStatusRequest.newBuilder()
        .setTxHash(ByteString.copyFrom(hash))
        .build();

    cmdStreamingStub.statusStream(req, new StreamObserverToSubject<>(subject));

    return subject;
  }

  public QueryResponse query(Queries.Query query) {
    return queryStub.find(query);
  }

  public Subject<BlockQueryResponse> blocksQuery(Queries.BlocksQuery query) {
    PublishSubject<BlockQueryResponse> subject = PublishSubject.create();
    queryStreamingStub.fetchCommits(query, new StreamObserverToSubject<>(subject));
    return subject;
  }

}
