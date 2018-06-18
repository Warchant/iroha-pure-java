package jp.co.soramitsu.iroha.java;

import iroha.protocol.Commands.Command;
import iroha.protocol.Commands.CreateAccount;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper;
import jp.co.soramitsu.iroha.java.detail.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.Signable;
import jp.co.soramitsu.iroha.java.detail.mapping.PubkeyMapper;

public class TransactionBuilder {

  private Transaction tx = new Transaction();

  /**
   * Both fields are required, therefore we can not create builder without them.
   */
  public TransactionBuilder(String accountId, Instant time) {
    setCreatorAccountId(accountId);
    setCreatedTime(time);
    setQuorum(1 /* default value for quorum */);
  }

  public TransactionBuilder setCreatorAccountId(String accountId) {
    tx.payload.setCreatorAccountId(accountId);
    return this;
  }

  public TransactionBuilder setCreatedTime(Instant time) {
    tx.payload.setCreatedTime(TimestampMapper.toProtobufValue(time));
    return this;
  }

  public TransactionBuilder setQuorum(int quorum) {
    tx.payload.setQuorum(quorum);
    return this;
  }

  public TransactionBuilder createAccount(String accountName, String domainid,
      PublicKey publicKey) {

    tx.payload.addCommands(
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

  public byte[] payload() {
    return tx.payload();
  }

  public byte[] hash() {
    return tx.hash();
  }

  public Signable sign(KeyPair keyPair) throws CryptoException {
    return tx.sign(keyPair);
  }

  public Transaction build() {
    return tx;
  }
}
