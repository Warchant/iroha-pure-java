package jp.co.soramitsu.iroha.java;

import iroha.protocol.Queries;
import iroha.protocol.Queries.Query.Payload;
import iroha.protocol.Queries.QueryPayloadMeta;
import java.security.KeyPair;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.Hashable;

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
    updatePayload();
    q.setSignature(Utils.sign(this, keyPair));
    return q.build();
  }

  public Queries.Query buildUnsigned() {
    updatePayload();
    return q.build();
  }

  public static QueryBuilder builder(String accountId, Instant time, long counter) {
    return new QueryBuilder(accountId, time, counter);
  }

  public static QueryBuilder builder(String accountId, long counter) {
    return new QueryBuilder(accountId, Instant.now(), counter);
  }
}
