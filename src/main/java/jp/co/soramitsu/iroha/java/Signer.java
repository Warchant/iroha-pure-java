package jp.co.soramitsu.iroha.java;

import static com.google.protobuf.ByteString.copyFrom;

import iroha.protocol.Primitive.Signature;
import iroha.protocol.Queries.BlocksQuery;
import iroha.protocol.Queries.Query;
import iroha.protocol.Queries.QueryPayloadMeta;
import iroha.protocol.TransactionOuterClass;
import iroha.protocol.TransactionOuterClass.Transaction.Payload;
import iroha.protocol.TransactionOuterClass.Transaction.Payload.ReducedPayload;
import java.security.KeyPair;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import lombok.val;
import org.spongycastle.jcajce.provider.digest.SHA3;

public class Signer {

  private static Ed25519Sha3 ed;
  private static SHA3.Digest256 digest = new SHA3.Digest256();
  private KeyPair keyPair;

  static {
    initEd();
  }

  public Signer(KeyPair keyPair) {
    this.keyPair = keyPair;
  }

  private static void initEd() {
    try {
      ed = new Ed25519Sha3();
    } catch (CryptoException e) {
      throw new IllegalStateException(e);
    }
  }

  public Query sign(
      Query.Payload.Builder queryBuilder,
      String queryCreator,
      Instant createdAt
  ) throws CryptoException {
    val payloadBuilder = queryBuilder
        .setMeta(
            QueryPayloadMeta
                .newBuilder()
                .setCreatorAccountId(queryCreator)
                .setCreatedTime(createdAt.toEpochMilli())
                .setQueryCounter(1)
                .build()
        );
    return Query.newBuilder()
        .setSignature(createSignature(payloadBuilder.buildPartial().toByteArray()))
        .setPayload(payloadBuilder)
        .build();
  }

  public BlocksQuery sign(
      String queryCreator,
      Instant createdAt
  ) throws CryptoException {
    val builder = QueryPayloadMeta
        .newBuilder()
        .setCreatorAccountId(queryCreator)
        .setQueryCounter(1)
        .setCreatedTime(createdAt.toEpochMilli());
    return BlocksQuery.newBuilder()
        .setSignature(createSignature(builder.buildPartial().toByteArray()))
        .setMeta(builder)
        .build();
  }

  public TransactionOuterClass.Transaction sign(
      ReducedPayload.Builder reducedPayload,
      Instant createdAt,
      String txCreator
  ) throws CryptoException {
    val payloadBuilder = Payload
        .newBuilder()
        .setReducedPayload(
            reducedPayload
                .setCreatorAccountId(txCreator)
                .setQuorum(1)
                .setCreatedTime(createdAt.toEpochMilli())
        );
    val tx = TransactionOuterClass.Transaction.newBuilder();
    return tx
        .addSignatures(createSignature(payloadBuilder.buildPartial().toByteArray()))
        .setPayload(payloadBuilder)
        .build();
  }

  public Signature createSignature(byte[] byteArray)
      throws CryptoException {
    val rawSignature = ed.rawSign(digest.digest(byteArray), keyPair);

    return Signature.newBuilder()
        .setSignature(copyFrom(rawSignature))
        .setPubkey(copyFrom(keyPair.getPublic().getEncoded()))
        .build();
  }

}
