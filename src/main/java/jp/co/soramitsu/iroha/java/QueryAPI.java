package jp.co.soramitsu.iroha.java;

import iroha.protocol.QryResponses.AccountResponse;
import iroha.protocol.QryResponses.TransactionsPageResponse;
import iroha.protocol.QryResponses.TransactionsResponse;
import java.security.KeyPair;
import java.util.List;
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

  public AccountResponse getAccount(String accountId) {
    val q = Query.builder(this.accountId, counter.getAndIncrement())
        .getAccount(accountId)
        .buildSigned(keyPair);

    val res = api.query(q);

    return res.getAccountResponse();
  }

  public TransactionsPageResponse getAccountTransactions(String accountId, Integer pageSize,
      String firstHashHex) {
    val q = Query.builder(this.accountId, counter.getAndIncrement())
        .getAccountTransactions(accountId, pageSize, firstHashHex)
        .buildSigned(keyPair);

    val res = api.query(q);

    return res.getTransactionsPageResponse();
  }

  public TransactionsPageResponse getAccountTransactions(String accountId, Integer pageSize) {
    return getAccountTransactions(accountId, pageSize, null);
  }

  public TransactionsPageResponse getAccountAssetTransactions(String accountId, String assetId,
      Integer pageSize, String firstHashHex) {
    val q = Query.builder(this.accountId, counter.getAndIncrement())
        .getAccountAssetTransactions(accountId, assetId, pageSize, firstHashHex)
        .buildSigned(keyPair);

    val res = api.query(q);

    return res.getTransactionsPageResponse();
  }

  public TransactionsPageResponse getAccountAssetTransactions(String accountId, String assetId,
      Integer pageSize) {
    return getAccountAssetTransactions(accountId, assetId, pageSize, null);
  }

  public TransactionsResponse getTransactions(List<byte[]> hashes) {
    val q = Query.builder(this.accountId, counter.getAndIncrement())
        .getTransactions(hashes)
        .buildSigned(keyPair);

    val res = api.query(q);

    return res.getTransactionsResponse();
  }
}
