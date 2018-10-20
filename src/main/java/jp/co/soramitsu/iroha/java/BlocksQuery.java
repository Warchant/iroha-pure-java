package jp.co.soramitsu.iroha.java;

import iroha.protocol.Queries;
import iroha.protocol.Queries.Query.Payload;
import iroha.protocol.Queries.QueryPayloadMeta;
import java.security.KeyPair;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.Hashable;

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
    updatePayload();
    q.setSignature(Utils.sign(this, keyPair));
    return q.build();
  }

  public Queries.BlocksQuery buildUnsigned() {
    updatePayload();
    return q.build();
  }

  public static BlocksQueryBuilder builder(String accountId, Instant time, long counter) {
    return new BlocksQueryBuilder(accountId, time, counter);
  }

  public static BlocksQueryBuilder builder(String accountId, long counter) {
    return builder(accountId, Instant.now(), counter);
  }
}
