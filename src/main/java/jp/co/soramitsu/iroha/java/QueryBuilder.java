package jp.co.soramitsu.iroha.java;

import static java.util.Objects.nonNull;

import iroha.protocol.Queries.GetAccount;
import iroha.protocol.Queries.GetAccountAssetTransactions;
import iroha.protocol.Queries.GetAccountAssets;
import iroha.protocol.Queries.GetAccountDetail;
import iroha.protocol.Queries.GetSignatories;
import iroha.protocol.Queries.QueryPayloadMeta;
import java.time.Instant;
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper;

public class QueryBuilder {

  private FieldValidator validator;

  private QueryPayloadMeta.Builder meta = QueryPayloadMeta.newBuilder();

  private Query newQuery() {
    return new Query(meta);
  }

  public QueryBuilder(String accountId, Instant time, long counter) {
    setCreatorAccountId(accountId);
    setCreatedTime(time);
    setCounter(counter);
  }

  public QueryBuilder enableValidation() {
    this.validator = new FieldValidator();
    return this;
  }

  public QueryBuilder disableValidation() {
    this.validator = null;
    return this;
  }

  public QueryBuilder setCreatorAccountId(String accountId) {
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    meta.setCreatorAccountId(accountId);
    return this;
  }

  public QueryBuilder setCreatedTime(Instant time) {
    if (nonNull(this.validator)) {
      this.validator.checkTimestamp(time);
    }

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
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
      this.validator.checkAssetId(assetId);
    }

    Query query = newQuery();

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
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    Query query = newQuery();

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
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    Query query = newQuery();

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
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    Query query = newQuery();

    query.getProto().setGetAccountAssets(
        GetAccountAssets.newBuilder()
            .setAccountId(accountId)
            .build()
    );

    return query;
  }

  public Query getAccountDetail(
      String accountId,
      String key
  ) {
    Query query = newQuery();

    query.getProto().setGetAccountDetail(
        GetAccountDetail.newBuilder()
            .setAccountId(accountId)
            .setKey(key)
            .build()
    );

    return query;
  }
}
