package jp.co.soramitsu.iroha.java;

import iroha.protocol.TransactionOuterClass;
import iroha.protocol.TransactionOuterClass.Transaction.Payload;
import iroha.protocol.TransactionOuterClass.Transaction.Payload.ReducedPayload;
import java.security.KeyPair;
import java.time.Instant;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;
import jp.co.soramitsu.iroha.java.detail.Hashable;

public class Transaction
    extends
    Hashable<TransactionOuterClass.Transaction.Payload.Builder>  // should be Payload.Builder
    implements BuildableAndSignable<TransactionOuterClass.Transaction> {

  private TransactionOuterClass.Transaction.Builder tx = TransactionOuterClass.Transaction
      .newBuilder();

  /* default */ ReducedPayload.Builder reducedPayload = ReducedPayload.newBuilder();

  /* default */ void updatePayload() {
    tx.setPayload(
        getProto()
            .setReducedPayload(reducedPayload)
    );
  }

  /* default */ Transaction() {
    super(Payload.newBuilder());
  }

  @Override
  public BuildableAndSignable<TransactionOuterClass.Transaction> sign(KeyPair keyPair) {
    updatePayload();

    tx.addSignatures(Utils.sign(this, keyPair));

    return this;
  }

  @Override
  public TransactionOuterClass.Transaction build() {
    updatePayload();
    return tx.build();
  }

  public static TransactionBuilder builder(String accountId, Instant time) {
    return new TransactionBuilder(accountId, time);
  }

  public static TransactionBuilder builder(String accountId) {
    return builder(accountId, Instant.now());
  }
}
