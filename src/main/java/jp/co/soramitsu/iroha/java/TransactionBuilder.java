package jp.co.soramitsu.iroha.java;

import iroha.protocol.TransactionOuterClass;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;

public interface TransactionBuilder {

  TransactionBuilder setCreatorAccountId(String accountId);

  TransactionBuilder setCreatedTime(Instant time);

  TransactionBuilder setQuorum(int quorum);

  TransactionBuilder createAccount(
      String accountName,
      String domainid,
      PublicKey publicKey
  );

  TransactionBuilder transferAsset(
      String sourceAccount,
      String destinationAccount,
      String assetId,
      String description,
      BigDecimal amount
  );

  TransactionBuilder setAccountDetail(
      String accountId,
      String key,
      String value
  );

  BuildableAndSignable<TransactionOuterClass.Transaction> sign(KeyPair keyPair)
      throws CryptoException;

  Transaction build();
}
