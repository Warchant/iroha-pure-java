package jp.co.soramitsu.iroha.java;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.privateKeyFromBytes;
import static jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.publicKeyFromBytes;

import com.google.protobuf.ByteString;
import iroha.protocol.BlockOuterClass.Block;
import iroha.protocol.BlockOuterClass.Block_v1;
import iroha.protocol.Endpoint.TxList;
import iroha.protocol.Endpoint.TxStatusRequest;
import iroha.protocol.Primitive;
import iroha.protocol.Primitive.Signature;
import iroha.protocol.Queries;
import iroha.protocol.TransactionOuterClass;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.detail.Hashable;
import lombok.val;
import org.spongycastle.jcajce.provider.digest.SHA3;

public class Utils {

  /**
   * @deprecated use {@code Utils.parseHexKeypair}
   */
  @Deprecated
  public static KeyPair keyPair(String hexPublicKey, String hexPrivateKey) {
    return parseHexKeypair(hexPublicKey, hexPrivateKey);
  }

  public static KeyPair parseHexKeypair(String hexPublicKey, String hexPrivateKey) {
    return new KeyPair(
        parseHexPublicKey(hexPublicKey),
        parseHexPrivateKey(hexPrivateKey)
    );
  }

  public static PublicKey parseHexPublicKey(String hexPublicKey) {
    return publicKeyFromBytes(parseHexBinary(hexPublicKey));
  }

  public static PrivateKey parseHexPrivateKey(String hexPrivateKey) {
    return privateKeyFromBytes(parseHexBinary(hexPrivateKey));
  }

  public static byte[] hash(TransactionOuterClass.Transaction tx) {
    val sha3 = new SHA3.Digest256();
    val data = tx.getPayload().toByteArray();
    return sha3.digest(data);
  }

  public static byte[] hash(Block_v1 b) {
    val sha3 = new SHA3.Digest256();
    val data = b.getPayload().toByteArray();
    return sha3.digest(data);
  }

  public static byte[] hash(Block b) {
    switch (b.getBlockVersionCase()) {
      case BLOCK_V1:
        return hash(b.getBlockV1());
      default:
        throw new IllegalArgumentException(
            String.format("Block has undefined version: %s", b.getBlockVersionCase()));
    }
  }

  public static byte[] hash(Queries.Query q) {
    val sha3 = new SHA3.Digest256();
    val data = q.getPayload().toByteArray();
    return sha3.digest(data);
  }

  /* default */
  static <T extends Hashable> Primitive.Signature sign(T t, KeyPair kp) {
    byte[] rawSignature = new Ed25519Sha3().rawSign(t.hash(), kp);

    return Signature.newBuilder()
        .setSignature(
            ByteString.copyFrom(rawSignature)
        )
        .setPublicKey(
            ByteString.copyFrom(kp.getPublic().getEncoded())
        )
        .build();
  }

  // this method is here only because some old versions of Android do not have Objects.nonNull
  public static boolean nonNull(Object obj) {
    return obj != null;
  }

  public static TxStatusRequest createTxStatusRequest(byte[] hash) {
    return TxStatusRequest.newBuilder()
        .setTxHash(ByteString.copyFrom(hash))
        .build();
  }

  public static TxList createTxList(Iterable<TransactionOuterClass.Transaction> list) {
    return TxList.newBuilder()
        .addAllTransactions(list)
        .build();
  }
}
