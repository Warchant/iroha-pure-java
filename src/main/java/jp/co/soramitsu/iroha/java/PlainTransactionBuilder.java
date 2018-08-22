package jp.co.soramitsu.iroha.java;

import iroha.protocol.Commands.Command;
import iroha.protocol.Commands.CreateAccount;
import iroha.protocol.Commands.SetAccountDetail;
import iroha.protocol.Commands.TransferAsset;
import iroha.protocol.TransactionOuterClass;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;
import jp.co.soramitsu.iroha.java.detail.mapping.PubkeyMapper;
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper;

public class PlainTransactionBuilder implements TransactionBuilder {

  private Transaction tx = new Transaction();

  /**
   * Both fields are required, therefore we can not create builder without them.
   */
  public PlainTransactionBuilder(String accountId, Instant time) {
    setCreatorAccountId(accountId);
    setCreatedTime(time);
    setQuorum(1 /* default value for quorum */);
  }

  @Override
  public TransactionBuilder setCreatorAccountId(String accountId) {
    tx.getProto().setCreatorAccountId(accountId);
    return this;
  }

  @Override
  public TransactionBuilder setCreatedTime(Instant time) {
    tx.getProto().setCreatedTime(TimestampMapper.toProtobufValue(time));
    return this;
  }

  @Override
  public TransactionBuilder setQuorum(int quorum) {
    tx.getProto().setQuorum(quorum);
    return this;
  }

  @Override
  public TransactionBuilder createAccount(
      String accountName,
      String domainid,
      PublicKey publicKey
  ) {
    tx.getProto().addCommands(
        Command.newBuilder()
            .setCreateAccount(
                CreateAccount.newBuilder()
                    .setAccountName(accountName)
                    .setDomainId(domainid)
                    .setMainPubkey(
                        PubkeyMapper.toProtobufValue(publicKey)
                    ).build()
            ).build()
    );

    return this;
  }

  @Override
  public TransactionBuilder transferAsset(
      String sourceAccount,
      String destinationAccount,
      String assetId,
      String description,
      BigDecimal amount
  ) {
    tx.getProto().addCommands(
        Command.newBuilder()
            .setTransferAsset(
                TransferAsset.newBuilder()
                    .setSrcAccountId(sourceAccount)
                    .setDestAccountId(destinationAccount)
                    .setAssetId(assetId)
                    .setDescription(description)
                    .setAmount(amount.toPlainString())
                    .build()
            ).build()
    );

    return this;
  }

  @Override
  public TransactionBuilder setAccountDetail(
      String accountId,
      String key,
      String value
  ) {
    tx.getProto().addCommands(
        Command.newBuilder()
            .setSetAccountDetail(
                SetAccountDetail.newBuilder()
                    .setAccountId(accountId)
                    .setKey(key)
                    .setValue(value)
                    .build()
            )
            .build()
    );

    return this;
  }

  @Override
  public BuildableAndSignable<TransactionOuterClass.Transaction> sign(KeyPair keyPair)
      throws CryptoException {
    return tx.sign(keyPair);
  }

  @Override
  public Transaction build() {
    return tx;
  }
}
