package jp.co.soramitsu.iroha.java;

import com.google.protobuf.ByteString;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.BlockOuterClass.Transaction.Payload;
import iroha.protocol.Primitive.Signature;
import java.security.KeyPair;
import java.time.Instant;
import jp.co.soramitsu.iroha.java.detail.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.detail.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.ProtobufBuildable;
import jp.co.soramitsu.iroha.java.detail.Signable;
import jp.co.soramitsu.iroha.java.detail.mapping.PubkeyMapper;
import org.spongycastle.jcajce.provider.digest.SHA3;


public class Transaction implements Signable, ProtobufBuildable {

  private SHA3.Digest256 digest = new SHA3.Digest256();

  private BlockOuterClass.Transaction.Builder tx = BlockOuterClass.Transaction.newBuilder();

  /* default */ Payload.Builder payload = Payload.newBuilder();

  public byte[] hash() {
    return digest.digest(payload());
  }

  public byte[] payload() {
    return payload.buildPartial().toByteArray();
  }

  @Override
  public Signable sign(KeyPair keyPair) throws CryptoException {
    Ed25519Sha3 ed = new Ed25519Sha3();  // throws

    tx.setPayload(payload);

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
