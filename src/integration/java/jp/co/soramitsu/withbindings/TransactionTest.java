package jp.co.soramitsu.withbindings;

import static jp.co.soramitsu.withbindings.ByteVectorUtil.byteVector2bytes;
import static jp.co.soramitsu.withbindings.ByteVectorUtil.bytes2Blob;
import static org.junit.Assert.assertEquals;

import com.google.protobuf.InvalidProtocolBufferException;
import iroha.protocol.Primitive.GrantablePermission;
import iroha.protocol.Primitive.RolePermission;
import iroha.protocol.TransactionOuterClass;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.Grantable;
import jp.co.soramitsu.iroha.Keypair;
import jp.co.soramitsu.iroha.ModelTransactionBuilder;
import jp.co.soramitsu.iroha.PrivateKey;
import jp.co.soramitsu.iroha.PublicKey;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper;
import lombok.val;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class TransactionTest {

  static {
    Configuration.init();
  }

  private final String privateKey = "0f0ce16d2afbb8eca23c7d8c2724f0c257a800ee2bbd54688cec6b898e3f7e33";
  private final String publicKey = "889f6b881e331be21487db77dcf32c5f8d3d5e8066e78d2feac4239fe91d416f";
  private final String accountName = "account";
  private final String domainId = "domain";
  private final String accountId = "vasya@pupkin";
  private final String srcAccountId = "accounta@domain";
  private final String dstAccountId = "accountb@domain";
  private final String assetId = "asset#id";
  private final String description = "description?";
  private final String key = "key";
  private final String value = "value";
  private final String peerAddress = "0.0.0.0:10001";
  private final String defaultRole = "role";
  private final ArrayList<RolePermission> permissions = new ArrayList<>(
      Arrays.asList(RolePermission.values()));

  private final BigDecimal amount = BigDecimal.TEN;
  private final Instant instant = Instant.now();
  private final Long time = TimestampMapper.toProtobufValue(instant);


  private TransactionOuterClass.Transaction createPureJavaTx(byte[] publicKey, byte[] privateKey)
      throws CryptoException {
    val keyPair = Ed25519Sha3.keyPairFromBytes(privateKey, publicKey);

    val tx = Transaction.builder(accountId, instant)
        .createAccount(accountName, domainId, keyPair.getPublic())
        .transferAsset(srcAccountId, dstAccountId, assetId, description, amount)
        .addPeer(peerAddress, Hex.decode(publicKey))
        .setAccountDetail(accountId, key, value)
        .createDomain(domainId, defaultRole)
        // TODO(@warchant): figure out how to add createRole cmd in bindings
        // .createRole(defaultRole, permissions)
        .appendRole(accountId, defaultRole)
        .grantPermission(accountId, GrantablePermission.can_set_my_account_detail)
        .sign(keyPair)
        .build();

    return tx;
  }

  private TransactionOuterClass.Transaction createIrohalibTx(byte[] publicKey, byte[] privateKey)
      throws InvalidProtocolBufferException {

    val keyPair = new Keypair(
        new PublicKey(bytes2Blob(publicKey)),
        new PrivateKey(bytes2Blob(privateKey))
    );

    val blob = new ModelTransactionBuilder()
        .creatorAccountId(accountId)
        .createdTime(BigInteger.valueOf(time))
        .createAccount(accountName, domainId, keyPair.publicKey())
        .transferAsset(srcAccountId, dstAccountId, assetId, description, amount.toString())
        .addPeer(peerAddress, keyPair.publicKey())
        .setAccountDetail(accountId, key, value)
        .createDomain(domainId, defaultRole)
        .appendRole(accountId, defaultRole)
        .grantPermission(accountId, Grantable.kSetMyAccountDetail)
        .build()
        .signAndAddSignature(keyPair)
        .finish()
        .blob()
        .blob();

    return TransactionOuterClass.Transaction.parseFrom(byteVector2bytes(blob));
  }

  @Test
  public void createTwoTxWithJavaAndBindings()
      throws CryptoException, InvalidProtocolBufferException {

    val bytesPriv = DatatypeConverter.parseHexBinary(privateKey);
    val bytesPub = DatatypeConverter.parseHexBinary(publicKey);

    val tx2 = createIrohalibTx(bytesPub, bytesPriv);
    val tx1 = createPureJavaTx(bytesPub, bytesPriv);

    assertEquals(tx1, tx2);
  }

}
