package jp.co.soramitsu.iroha.java;

import iroha.protocol.BlockOuterClass;
import iroha.protocol.BlockOuterClass.Transaction.Payload;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;
import jp.co.soramitsu.iroha.java.detail.Hashable;

public class Transaction
    extends
    Hashable<BlockOuterClass.Transaction.Payload.Builder>  // should be Payload.Builder
    implements BuildableAndSignable<BlockOuterClass.Transaction> {

  private BlockOuterClass.Transaction.Builder tx = BlockOuterClass.Transaction
      .newBuilder();

  /* default */ void updatePayload() {
    tx.setPayload(getProto());
  }

  /* default */ Transaction() {
    super(Payload.newBuilder());
  }

  @Override
  public BuildableAndSignable<BlockOuterClass.Transaction> sign(KeyPair keyPair)
      throws CryptoException {
    updatePayload();

    tx.addSignatures(Utils.sign(this, keyPair));

    return this;
  }

  @Override
  public BlockOuterClass.Transaction build() {
    updatePayload();
    return tx.build();
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
    return builder(accountId, Instant.now());
  }
}
