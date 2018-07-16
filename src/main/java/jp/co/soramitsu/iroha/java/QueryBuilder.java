package jp.co.soramitsu.iroha.java;

import iroha.protocol.Queries.GetAccount;
import iroha.protocol.Queries.GetAccountAssetTransactions;
import iroha.protocol.Queries.GetAccountAssets;
import iroha.protocol.Queries.GetSignatories;
import iroha.protocol.Queries.QueryPayloadMeta;
import java.time.Instant;
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper;

public class QueryBuilder {

  private QueryPayloadMeta.Builder meta = QueryPayloadMeta.newBuilder();

  private Query query = new Query(meta);

  public QueryBuilder(String accountId, Instant time, long counter) {
    setCreatorAccountId(accountId);
    setCreatedTime(time);
    setCounter(counter);
  }

  public QueryBuilder setCreatorAccountId(String accountId) {
    meta.setCreatorAccountId(accountId);
    return this;
  }

  public QueryBuilder setCreatedTime(Instant time) {
    meta.setCreatedTime(TimestampMapper.toProtobufValue(time));
    return this;
  }

  public QueryBuilder setCounter(long counter) {
    meta.setQueryCounter(counter);
    return this;
  }

  public Query getAccountAssetTransactions(
      String accountId,
      String assetId
  ) {
    query.getProto().setGetAccountAssetTransactions(
        GetAccountAssetTransactions.newBuilder()
            .setAccountId(accountId)
            .setAssetId(assetId)
            .build()
    );

    return query;
  }

  public Query getAccount(
      String accountId
  ) {
    query.getProto().setGetAccount(
        GetAccount.newBuilder()
            .setAccountId(accountId)
            .build()
    );

    return query;
  }

  public Query getSignatories(
      String accountId
  ) {
    query.getProto().setGetAccountSignatories(
        GetSignatories.newBuilder()
            .setAccountId(accountId)
            .build()
    );

    return query;
  }

  public Query getAccountAssets(
      String accountId
  ) {
    query.getProto().setGetAccountAssets(
        GetAccountAssets.newBuilder()
            .setAccountId(accountId)
            .build()
    );

    return query;
  }
}
