package jp.co.soramitsu.iroha.java.detail;

import com.google.protobuf.ByteString;
import iroha.protocol.Endpoint.TxList;
import iroha.protocol.Endpoint.TxStatusRequest;
import iroha.protocol.TransactionOuterClass;

public class ProtoUtils {

  public static TxStatusRequest createTxStatusRequest(byte[] hash) {
    return TxStatusRequest.newBuilder()
        .setTxHash(ByteString.copyFrom(hash))
        .build();
  }

  public static TxList createTxList(Iterable<TransactionOuterClass.Transaction> list) {
    return TxList.newBuilder()
        .addAllTransactions(list)
        .build();
  }
}
