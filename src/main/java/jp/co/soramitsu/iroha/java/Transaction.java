package jp.co.soramitsu.iroha.java;

import iroha.protocol.TransactionOuterClass;
import iroha.protocol.TransactionOuterClass.Transaction.Payload;
import iroha.protocol.TransactionOuterClass.Transaction.Payload.ReducedPayload;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;
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

  /* default */ Transaction(TransactionOuterClass.Transaction tx) {
    super(Payload.newBuilder(tx.getPayload()));
    this.tx = TransactionOuterClass.Transaction.newBuilder(tx);
    this.reducedPayload = ReducedPayload.newBuilder(tx.getPayload().getReducedPayload());
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

  public static Transaction parseFromProto(TransactionOuterClass.Transaction input) {
    return new Transaction(input);
  }

  public static TransactionBuilder builder(String accountId, Long date) {
    return new TransactionBuilder(accountId, date);
  }

  public static TransactionBuilder builder(String accountId, Date date) {
    return new TransactionBuilder(accountId, date);
  }

  public static TransactionBuilder builder(String accountId, Instant time) {
    return new TransactionBuilder(accountId, time);
  }

  public static TransactionBuilder builder(String accountId) {
    return builder(accountId, System.currentTimeMillis());
  }
}
