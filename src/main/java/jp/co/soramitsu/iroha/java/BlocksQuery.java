package jp.co.soramitsu.iroha.java;

import com.google.protobuf.ByteString;
import iroha.protocol.Primitive.Signature;
import iroha.protocol.Queries;
import iroha.protocol.Queries.Query.Payload;
import iroha.protocol.Queries.QueryPayloadMeta;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.Hashable;
import jp.co.soramitsu.iroha.java.detail.mapping.PubkeyMapper;

import java.security.KeyPair;
import java.time.Instant;

public class BlocksQuery
    extends Hashable<Payload.Builder> {

  private QueryPayloadMeta.Builder meta;
  private Queries.BlocksQuery.Builder q = Queries.BlocksQuery.newBuilder();

  public BlocksQuery(QueryPayloadMeta.Builder meta) {
    super(Payload.newBuilder());

    this.meta = meta;
  }

  public byte[] payload() {
    return meta.buildPartial().toByteArray();
  }

  private void updatePayload() {
    getProto().setMeta(meta);
    q.setMeta(meta);
  }

  public Queries.BlocksQuery buildSigned(KeyPair keyPair) throws CryptoException {
    Ed25519Sha3 ed = new Ed25519Sha3();
    updatePayload();
    byte[] rawSignature = ed.rawSign(hash(), keyPair);

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

  public Queries.BlocksQuery buildUnsigned() {
    updatePayload();
    return q.build();
  }

  public static BlocksQueryBuilder builder(String accountId, Instant time, long counter){
    return new BlocksQueryBuilder(accountId, time, counter);
  }
}
