package jp.co.soramitsu.iroha.java;

import java.security.KeyPair;
import java.util.concurrent.atomic.AtomicInteger;
import jp.co.soramitsu.iroha.java.debug.Account;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Getter
public class QueryAPI {

  @NonNull
  private IrohaAPI api;
  @NonNull
  private String accountId;
  @NonNull
  private KeyPair keyPair;

  public QueryAPI(IrohaAPI api, String accountId, KeyPair keyPair) {
    this.api = api;
    this.accountId = accountId;
    this.keyPair = keyPair;
  }

  public QueryAPI(IrohaAPI api, Account account) {
    this.api = api;
    this.accountId = account.getId();
    this.keyPair = account.getKeyPair();
  }

  private AtomicInteger counter = new AtomicInteger(1);

  public String getAccountDetails(
      String accountId,
      String writer,
      String key
  ) {
    val q = Query.builder(this.accountId, counter.getAndIncrement())
        .getAccountDetail(accountId, writer, key)
        .buildSigned(keyPair);

    val res = api.query(q);

    val adr = res.getAccountDetailResponse();

    return adr.getDetail();
  }

}
