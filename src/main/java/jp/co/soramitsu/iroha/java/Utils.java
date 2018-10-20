package jp.co.soramitsu.iroha.java;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.privateKeyFromBytes;
import static jp.co.soramitsu.crypto.ed25519.Ed25519Sha3.publicKeyFromBytes;

import com.google.protobuf.ByteString;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive;
import iroha.protocol.Primitive.Signature;
import iroha.protocol.Queries;
import iroha.protocol.TransactionOuterClass;
import java.security.KeyPair;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.detail.Hashable;
import lombok.val;
import org.spongycastle.jcajce.provider.digest.SHA3;

public class Utils {

  public static KeyPair keyPair(String hexPublicKey, String hexPrivateKey) {
    return new KeyPair(
        publicKeyFromBytes(parseHexBinary(hexPublicKey)),
        privateKeyFromBytes(parseHexBinary(hexPrivateKey)));
  }

  public static byte[] hash(TransactionOuterClass.Transaction tx) {
    val sha3 = new SHA3.Digest256();
    val data = tx.getPayload().toByteArray();
    return sha3.digest(data);
  }

  public static byte[] hash(BlockOuterClass.Block b) {
    val sha3 = new SHA3.Digest256();
    val data = b.getPayload().toByteArray();
    return sha3.digest(data);
  }

  public static byte[] hash(Queries.Query q) {
    val sha3 = new SHA3.Digest256();
    val data = q.getPayload().toByteArray();
    return sha3.digest(data);
  }

  static <T extends Hashable> Primitive.Signature sign(T t, KeyPair kp) {
    byte[] rawSignature = new Ed25519Sha3().rawSign(t.hash(), kp);

    return Signature.newBuilder()
        .setSignature(
            ByteString.copyFrom(rawSignature)
        )
        .setPubkey(
            ByteString.copyFrom(kp.getPublic().getEncoded())
        )
        .build();
  }
}
