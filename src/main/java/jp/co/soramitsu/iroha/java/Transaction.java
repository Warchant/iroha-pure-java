package jp.co.soramitsu.iroha.java;

import com.google.protobuf.ByteString;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.BlockOuterClass.Transaction.Payload;
import iroha.protocol.Primitive.Signature;
import java.security.KeyPair;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;
import jp.co.soramitsu.iroha.java.detail.Hashable;
import jp.co.soramitsu.iroha.java.detail.mapping.PubkeyMapper;


public class Transaction
    extends Hashable<BlockOuterClass.Transaction.Payload.Builder>
    implements BuildableAndSignable<BlockOuterClass.Transaction> {

  private BlockOuterClass.Transaction.Builder tx = BlockOuterClass.Transaction.newBuilder();

  private void updatePayload() {
    tx.setPayload(getProto());
  }

  /* default */ Transaction() {
    super(Payload.newBuilder());
  }

  @Override
  public BuildableAndSignable<BlockOuterClass.Transaction> sign(KeyPair keyPair)
      throws CryptoException {
    Ed25519Sha3 ed = new Ed25519Sha3();  // throws

    updatePayload();

    byte[] rawSignature = ed.rawSign(hash(), keyPair);  // throws

    Signature sig = Signature.newBuilder()
        .setSignature(
            ByteString.copyFrom(rawSignature)
        )
        .setPubkey(
            PubkeyMapper.toProtobufValue(keyPair.getPublic())
        )
        .build();

    tx.addSignatures(sig);

    return this;
  }


  @Override
  public BlockOuterClass.Transaction build() {
    return tx.build();
  }

  public static TransactionBuilder builder(String accountId, Instant time) {
    return new TransactionBuilder(accountId, time);
  }
}
