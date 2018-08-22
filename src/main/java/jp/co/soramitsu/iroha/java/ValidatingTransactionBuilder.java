package jp.co.soramitsu.iroha.java;

import iroha.protocol.TransactionOuterClass;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;
import lombok.NonNull;

public class ValidatingTransactionBuilder implements TransactionBuilder {

  private PlainTransactionBuilder delegate;

  public ValidatingTransactionBuilder(@NonNull String accountId, @NonNull Instant time) {
    this.delegate = new PlainTransactionBuilder(accountId, time);
  }

  @Override
  public TransactionBuilder setCreatorAccountId(@NonNull String accountId) {
    return delegate.setCreatorAccountId(accountId);
  }

  @Override
  public TransactionBuilder setCreatedTime(@NonNull Instant time) {
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
  public TransactionBuilder createAccount(
      @NonNull String accountName,
      @NonNull String domainid,
      @NonNull PublicKey publicKey
  ) {
    return delegate.createAccount(accountName, domainid, publicKey);
  }

  @Override
  public TransactionBuilder transferAsset(
      @NonNull String sourceAccount,
      @NonNull String destinationAccount,
      @NonNull String assetId,
      @NonNull String description,
      @NonNull BigDecimal amount
  ) {
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
  public TransactionBuilder setAccountDetail(
      @NonNull String accountId,
      @NonNull String key,
      @NonNull String value
  ) {
    return delegate.setAccountDetail(accountId, key, value);
  }

  @Override
  public BuildableAndSignable<TransactionOuterClass.Transaction> sign(@NonNull KeyPair keyPair)
      throws CryptoException {
    return delegate.sign(keyPair);
  }

  @Override
  public Transaction build() {
    return delegate.build();
  }

}
