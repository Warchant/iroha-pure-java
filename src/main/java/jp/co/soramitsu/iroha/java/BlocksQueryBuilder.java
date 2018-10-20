package jp.co.soramitsu.iroha.java;

import static java.util.Objects.nonNull;

import iroha.protocol.Queries.QueryPayloadMeta;
import java.time.Instant;

public class BlocksQueryBuilder {

  private FieldValidator validator;

  private QueryPayloadMeta.Builder meta = QueryPayloadMeta.newBuilder();

  private BlocksQuery newQuery() {
    return new BlocksQuery(meta);
  }

  public BlocksQueryBuilder(String accountId, Instant time, long counter) {
    setCreatorAccountId(accountId);
    setCreatedTime(time);
    setCounter(counter);
  }

  public BlocksQueryBuilder enableValidation() {
    this.validator = new FieldValidator();
    return this;
  }

  public BlocksQueryBuilder disableValidation() {
    this.validator = null;
    return this;
  }

  public BlocksQueryBuilder setCreatorAccountId(String accountId) {
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    meta.setCreatorAccountId(accountId);
    return this;
  }

  public BlocksQueryBuilder setCreatedTime(Instant time) {
    if (nonNull(this.validator)) {
      this.validator.checkTimestamp(time);
    }

    meta.setCreatedTime(time.toEpochMilli());
    return this;
  }

  public BlocksQueryBuilder setCounter(long counter) {
    meta.setQueryCounter(counter);
    return this;
  }

  public BlocksQuery getQuery() {
    System.out.println(meta);
    return newQuery();
  }
}
