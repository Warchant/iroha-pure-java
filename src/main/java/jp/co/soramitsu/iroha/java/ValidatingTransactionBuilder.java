package jp.co.soramitsu.iroha.java;

import static java.util.Objects.requireNonNull;

import iroha.protocol.TransactionOuterClass;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import java.util.function.Supplier;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;
import lombok.AllArgsConstructor;

public class ValidatingTransactionBuilder implements TransactionBuilder {

  private PlainTransactionBuilder delegate;

  public ValidatingTransactionBuilder(String accountId, Instant time) {
    this.delegate = new PlainTransactionBuilder(accountId, time);
  }

  @Override
  public TransactionBuilder setCreatorAccountId(String accountId) {
    requireNonNull(accountId, new StringNotNullMessageSupplier("accountId"));
    return delegate.setCreatorAccountId(accountId);
  }

  @Override
  public TransactionBuilder setCreatedTime(Instant time) {
    requireNonNull(time, new StringNotNullMessageSupplier("time"));
    return delegate.setCreatedTime(time);
  }

  @Override
  public TransactionBuilder setQuorum(int quorum) {
    if (quorum < 1) {
      throw new IllegalArgumentException("Quorum can't be less than or equal to zero");
    }
    return delegate.setQuorum(quorum);
  }

  @Override
  public TransactionBuilder createAccount(String accountName, String domainid,
      PublicKey publicKey) {
    requireNonNull(accountName, new StringNotNullMessageSupplier("accountName"));
    requireNonNull(domainid, new StringNotNullMessageSupplier("domainid"));
    requireNonNull(publicKey, new StringNotNullMessageSupplier("publicKey"));
    return delegate.createAccount(accountName, domainid, publicKey);
  }

  @Override
  public TransactionBuilder transferAsset(String sourceAccount, String destinationAccount,
      String assetId, String description, BigDecimal amount) {
    requireNonNull(sourceAccount, new StringNotNullMessageSupplier("sourceAccount"));
    requireNonNull(destinationAccount, new StringNotNullMessageSupplier("destinationAccount"));
    requireNonNull(assetId, new StringNotNullMessageSupplier("assetId"));
    validateAmount(amount);

    return delegate.transferAsset(sourceAccount, destinationAccount, assetId, description, amount);
  }

  private void validateAmount(BigDecimal amount) {
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("BigInteger must be positive");
    }
    if (amount.unscaledValue().bitLength() > 256) {
      throw new IllegalArgumentException("BigInteger does not fit into uint256");
    }
  }

  @Override
  public TransactionBuilder setAccountDetail(String accountId, String key, String value) {
    requireNonNull(accountId, new StringNotNullMessageSupplier("accountId"));
    requireNonNull(key, new StringNotNullMessageSupplier("key"));

    return delegate.setAccountDetail(accountId, key, value);
  }

  @Override
  public BuildableAndSignable<TransactionOuterClass.Transaction> sign(KeyPair keyPair)
      throws CryptoException {
    requireNonNull(keyPair, new StringNotNullMessageSupplier("keyPair"));
    return delegate.sign(keyPair);
  }

  @Override
  public Transaction build() {
    return delegate.build();
  }

  @AllArgsConstructor
  private static class StringNotNullMessageSupplier implements Supplier<String> {

    private String param;

    @Override
    public String get() {
      return param + " can't be null";
    }
  }
}
