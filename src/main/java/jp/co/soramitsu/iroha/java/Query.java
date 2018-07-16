package jp.co.soramitsu.iroha.java;

import com.google.protobuf.ByteString;
import iroha.protocol.Primitive.Signature;
import iroha.protocol.Queries;
import iroha.protocol.Queries.Query.Payload;
import iroha.protocol.Queries.QueryPayloadMeta;
import java.security.KeyPair;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.Hashable;
import jp.co.soramitsu.iroha.java.detail.mapping.PubkeyMapper;

public class Query
    extends Hashable<Payload.Builder> {

  private QueryPayloadMeta.Builder meta;
  private Queries.Query.Builder q = Queries.Query.newBuilder();

  public Query(QueryPayloadMeta.Builder meta) {
    super(Payload.newBuilder());

    this.meta = meta;
  }

  private void updatePayload() {
    getProto().setMeta(meta);
    q.setPayload(getProto());
  }

  public Queries.Query buildSigned(KeyPair keyPair) throws CryptoException {
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

    q.setSignature(sig);

    return q.build();
  }

  public Queries.Query buildUnsigned() {
    updatePayload();
    return q.build();
  }

  public static QueryBuilder builder(String accountId, Instant time, long counter){
    return new QueryBuilder(accountId, time, counter);
  }
}
