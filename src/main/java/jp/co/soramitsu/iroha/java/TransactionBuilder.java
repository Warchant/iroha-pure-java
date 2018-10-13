package jp.co.soramitsu.iroha.java;

import static java.util.Objects.nonNull;

import com.google.protobuf.ByteString;
import iroha.protocol.Commands.AddPeer;
import iroha.protocol.Commands.AppendRole;
import iroha.protocol.Commands.Command;
import iroha.protocol.Commands.CreateAccount;
import iroha.protocol.Commands.CreateDomain;
import iroha.protocol.Commands.CreateRole;
import iroha.protocol.Commands.GrantPermission;
import iroha.protocol.Commands.SetAccountDetail;
import iroha.protocol.Commands.TransferAsset;
import iroha.protocol.Primitive.GrantablePermission;
import iroha.protocol.Primitive.Peer;
import iroha.protocol.Primitive.RolePermission;
import iroha.protocol.TransactionOuterClass;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.java.detail.BuildableAndSignable;
import jp.co.soramitsu.iroha.java.detail.mapping.PubkeyMapper;
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper;

public class TransactionBuilder {

  private FieldValidator validator;
  private Transaction tx = new Transaction();

  /**
   * Both fields are required, therefore we can not create builder without them.
   */
  public TransactionBuilder(String accountId, Instant time) {
    // passing null throws but we sometimes want to pass null to set no-value, example - genesis block
    if (nonNull(accountId)) {
      setCreatorAccountId(accountId);
    }

    if (nonNull(time)) {
      setCreatedTime(time);
    }

    setQuorum(1 /* default value for quorum */);
  }

  public TransactionBuilder enableValidation() {
    this.validator = new FieldValidator();
    return this;
  }

  public TransactionBuilder disableValidation() {
    this.validator = null;
    return this;
  }

  public TransactionBuilder setCreatorAccountId(String accountId) {
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    tx.reducedPayload.setCreatorAccountId(accountId);
    return this;
  }

  public TransactionBuilder setCreatedTime(Instant time) {
    if (nonNull(this.validator)) {
      this.validator.checkTimestamp(time);
    }

    tx.reducedPayload.setCreatedTime(TimestampMapper.toProtobufValue(time));
    return this;
  }

  public TransactionBuilder setQuorum(int quorum) {
    if (nonNull(this.validator)) {
      this.validator.checkQuorum(quorum);
    }

    tx.reducedPayload.setQuorum(quorum);
    return this;
  }

  public TransactionBuilder createAccount(
      String accountName,
      String domainid,
      PublicKey publicKey
  ) {
    if (nonNull(this.validator)) {
      this.validator.checkAccount(accountName);
      this.validator.checkDomainId(domainid);
    }

    tx.reducedPayload.addCommands(
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

  public TransactionBuilder transferAsset(
      String sourceAccount,
      String destinationAccount,
      String assetId,
      String description,
      BigDecimal amount
  ) {
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(sourceAccount);
      this.validator.checkAccountId(destinationAccount);
      this.validator.checkAssetId(assetId);
    }

    tx.reducedPayload.addCommands(
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

  public TransactionBuilder setAccountDetail(
      String accountId,
      String key,
      String value
  ) {
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
      this.validator.checkAccountDetailsKey(key);
      this.validator.checkAccountDetailsValue(value);
    }

    tx.reducedPayload.addCommands(
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

  public TransactionBuilder addPeer(
      String address,
      byte[] peerKey
  ) {
    if (nonNull(this.validator)) {
      this.validator.checkPeerAddress(address);
      this.validator.checkPublicKey(peerKey);
    }

    tx.reducedPayload.addCommands(
        Command.newBuilder()
            .setAddPeer(
                AddPeer.newBuilder()
                    .setPeer(
                        Peer.newBuilder()
                            .setAddress(address)
                            .setPeerKey(ByteString.copyFrom(peerKey))
                    ).build()
            ).build()
    );

    return this;
  }

  public TransactionBuilder grantPermission(
      String accountId,
      GrantablePermission permission
  ) {
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    tx.reducedPayload.addCommands(
        Command.newBuilder()
            .setGrantPermission(
                GrantPermission.newBuilder()
                    .setAccountId(accountId)
                    .setPermission(permission)
                    .build()
            ).build()
    );

    return this;
  }

  public TransactionBuilder createRole(
      String roleName,
      Iterable<? extends RolePermission> permissions
  ) {

    tx.reducedPayload.addCommands(
        Command.newBuilder().setCreateRole(
            CreateRole.newBuilder()
                .setRoleName(roleName)
                .addAllPermissions(permissions)
                .build()
        ).build()
    );

    return this;
  }

  public TransactionBuilder createDomain(
      String domainId,
      String defaultRole
  ) {

    if (nonNull(this.validator)) {
      this.validator.checkDomainId(domainId);
    }

    tx.reducedPayload.addCommands(
        Command.newBuilder()
            .setCreateDomain(
                CreateDomain.newBuilder()
                    .setDomainId(domainId)
                    .setDefaultRole(defaultRole)
                    .build()
            )
            .build()
    );

    return this;
  }

  public TransactionBuilder appendRole(
      String accountId,
      String roleName
  ) {
    if (nonNull(this.validator)) {
      this.validator.checkAccountId(accountId);
    }

    tx.reducedPayload.addCommands(
        Command.newBuilder()
            .setAppendRole(
                AppendRole.newBuilder()
                    .setAccountId(accountId)
                    .setRoleName(roleName)
                    .build()
            )
            .build()
    );

    return this;
  }

  public BuildableAndSignable<TransactionOuterClass.Transaction> sign(KeyPair keyPair)
      throws CryptoException {
    return tx.sign(keyPair);
  }

  public Transaction build() {
    return tx;
  }
}
