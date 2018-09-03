package jp.co.soramitsu.withbindings;

import static jp.co.soramitsu.withbindings.ByteVectorUtil.bytes2Blob;
import static org.junit.Assert.assertEquals;

import com.google.protobuf.InvalidProtocolBufferException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.time.Instant;
import javax.xml.bind.DatatypeConverter;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.CryptoException;
import jp.co.soramitsu.iroha.Keypair;
import jp.co.soramitsu.iroha.ModelCrypto;
import jp.co.soramitsu.iroha.ModelQueryBuilder;
import jp.co.soramitsu.iroha.PrivateKey;
import jp.co.soramitsu.iroha.PublicKey;
import jp.co.soramitsu.iroha.java.BlocksQuery;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper;
import lombok.val;
import org.junit.Test;

public class QueryTest {

  static {
    Configuration.init();
  }

  private final String privateKey = "0f0ce16d2afbb8eca23c7d8c2724f0c257a800ee2bbd54688cec6b898e3f7e33";
  private final String publicKey = "889f6b881e331be21487db77dcf32c5f8d3d5e8066e78d2feac4239fe91d416f";

  private final byte[] bytesPrivateKey = DatatypeConverter.parseHexBinary(privateKey);
  private final byte[] bytesPublicKey = DatatypeConverter.parseHexBinary(publicKey);


  private final String accountName = "account";
  private final String domainId = "domain";
  private final String accountId = "vasya@pupkin";
  private final String srcAccountId = "accounta@domain";
  private final String dstAccountId = "accountb@domain";
  private final String assetId = "asset#id";
  private final String description = "description?";
  private final String key = "key";
  private final String value = "value";
  private final long counter = 1L;

  private final BigDecimal amount = BigDecimal.TEN;
  private final Instant instant = Instant.now();
  private final Long time = TimestampMapper.toProtobufValue(instant);

  private KeyPair keyPair1 = Ed25519Sha3.keyPairFromBytes(
      bytesPrivateKey,
      bytesPublicKey
  );

  private Keypair keyPair2 = new Keypair(
      new PublicKey(bytes2Blob(bytesPublicKey)),
      new PrivateKey(bytes2Blob(bytesPrivateKey))
  );

  private ModelQueryBuilder getQueryTemplate(){
    return new ModelQueryBuilder()
        .createdTime(BigInteger.valueOf(time))
        .queryCounter(BigInteger.valueOf(counter))
        .creatorAccountId(accountId);
  }

  @Test
  public void getAccountAssetTransactions() throws CryptoException, InvalidProtocolBufferException {
    val query1 = Query.builder(accountId, instant, counter)
            .getAccountAssetTransactions(accountId, assetId)
            .buildSigned(keyPair1);

    val query2 = ByteVectorUtil.queryFromBlob(getQueryTemplate()
            .getAccountAssetTransactions(accountId, assetId)
            .build()
            .signAndAddSignature(keyPair2)
            .finish()
            .blob()
            .blob());

    assertEquals(query1, query2);
  }

  @Test
  public void getAccountAssetTransactionsBlocks() throws CryptoException, InvalidProtocolBufferException {
    val query1 = BlocksQuery.builder(accountId, instant, counter)
            .getAccountAssetTransactions(accountId, assetId)
            .buildSigned(keyPair1);

    val query2 = ByteVectorUtil.blocksQueryFromBlob(getQueryTemplate()
            .getAccountAssetTransactions(accountId, assetId)
            .build()
            .signAndAddSignature(keyPair2)
            .finish()
            .blob()
            .blob());

    assertEquals(query1, query2);
  }

  @Test
  public void getAccount() throws CryptoException, InvalidProtocolBufferException {
    val query1 = Query.builder(accountId, instant, counter)
        .getAccount(accountId)
        .buildSigned(keyPair1);

    val query2 = ByteVectorUtil.queryFromBlob(getQueryTemplate()
        .getAccount(accountId)
        .build()
        .signAndAddSignature(keyPair2)
        .finish()
        .blob()
        .blob());

    assertEquals(query1, query2);
  }

  @Test
  public void getAccountAssets() throws CryptoException, InvalidProtocolBufferException {
    val query1 = Query.builder(accountId, instant, counter)
        .getAccountAssets(accountId)
        .buildSigned(keyPair1);

    val query2 = ByteVectorUtil.queryFromBlob(getQueryTemplate()
        .getAccountAssets(accountId)
        .build()
        .signAndAddSignature(keyPair2)
        .finish()
        .blob()
        .blob());

    assertEquals(query1, query2);
  }

  @Test
  public void getSignatories() throws CryptoException, InvalidProtocolBufferException {
    val query1 = Query.builder(accountId, instant, counter)
        .getSignatories(accountId)
        .buildSigned(keyPair1);

    val query2 = ByteVectorUtil.queryFromBlob(getQueryTemplate()
        .getSignatories(accountId)
        .build()
        .signAndAddSignature(keyPair2)
        .finish()
        .blob()
        .blob());

    assertEquals(query1, query2);
  }
}
