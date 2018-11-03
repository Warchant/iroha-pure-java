package jp.co.soramitsu.iroha.java;

import static java.util.Objects.nonNull;

import iroha.protocol.Queries.QueryPayloadMeta;
import java.time.Instant;
import java.util.Date;

public class BlocksQueryBuilder {

  private FieldValidator validator;

  private QueryPayloadMeta.Builder meta = QueryPayloadMeta.newBuilder();

  private BlocksQuery newQuery() {
    return new BlocksQuery(meta);
  }

  private void init(String accountId, Long time, long counter) {
    setCreatorAccountId(accountId);
    setCreatedTime(time);
    setCounter(counter);
  }

  public BlocksQueryBuilder(String accountId, Instant time, long counter) {
    init(accountId, time.toEpochMilli(), counter);
  }

  public BlocksQueryBuilder(String accountId, Date time, long counter) {
    init(accountId, time.getTime(), counter);
  }

  public BlocksQueryBuilder(String accountId, Long time, long counter) {
    init(accountId, time, counter);
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

  public BlocksQueryBuilder setCreatedTime(Long time) {
    if (nonNull(this.validator)) {
      this.validator.checkTimestamp(time);
    }

    meta.setCreatedTime(time);
    return this;
  }

  public BlocksQueryBuilder setCreatedTime(Instant time) {
    return setCreatedTime(time.toEpochMilli());
  }

  public BlocksQueryBuilder setCreatedTime(Date time) {
    return setCreatedTime(time.getTime());
  }

  public BlocksQueryBuilder setCounter(long counter) {
    meta.setQueryCounter(counter);
    return this;
  }

  public BlocksQuery getQuery() {
    return newQuery();
  }
}
