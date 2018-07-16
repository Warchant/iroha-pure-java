package jp.co.soramitsu.withbindings

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.java.detail.mapping.TimestampMapper
import spock.lang.Specification

import jp.co.soramitsu.iroha.Blob
import jp.co.soramitsu.iroha.ByteVector
import jp.co.soramitsu.iroha.Keypair
import jp.co.soramitsu.iroha.ModelTransactionBuilder
import jp.co.soramitsu.iroha.PrivateKey

import javax.xml.bind.DatatypeConverter
import java.time.Instant

class TransactionTest extends Specification {
    static {
        try {
            System.loadLibrary("irohajava")
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e)
            System.err.println(System.getProperty("java.library.path"))
            System.exit(1)
        }
    }

    final
    def privateKey = DatatypeConverter.parseHexBinary(
            "0f0ce16d2afbb8eca23c7d8c2724f0c257a800ee2bbd54688cec6b898e3f7e33")
    final
    def publicKey = DatatypeConverter.parseHexBinary(
            "889f6b881e331be21487db77dcf32c5f8d3d5e8066e78d2feac4239fe91d416f")
    final def accountName = "account"
    final def domainId = "domain"
    final def accountId = "vasya@pupkin"
    final def instant = Instant.now()
    final def time = TimestampMapper.toProtobufValue(instant)

    final def srcAccountId = "source@domain"
    final def dstAccountId = "dest@domain"
    final def assetId = "asset#id"
    final def description = "description?"

    final def amount = BigDecimal.ONE

    final def key = "key"
    final def value = "value"

    Blob bytes2Blob(byte[] b) {
        ByteVector bv = new ByteVector(b.length)
        for (int i = 0; i < b.length; i++) {
            bv.set(i, b[i])
        }

        return new Blob(bv)
    }

    byte[] byteVector2bytes(ByteVector blob) {
        byte[] array = new byte[(int) blob.size()]
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) blob.get(i)
        }

        return array
    }


    def "generate all commands using bindings and iroha-pure-java and compare"() {
        given:
        def bindings = {
            def keyPair = new Keypair(
                    new PublicKey(bytes2Blob(publicKey)),
                    new PrivateKey(bytes2Blob(privateKey))
            )

            def blob = new ModelTransactionBuilder()
                    .creatorAccountId(accountId)
                    .createdTime(BigInteger.valueOf(time))
                    .createAccount(accountName, domainId, keyPair.publicKey())
                    .transferAsset(srcAccountId, dstAccountId, assetId, description, amount.toString())
                    .build()
                    .signAndAddSignature(keyPair)
                    .finish()
                    .blob()
                    .blob()

            return BlockOuterClass.Transaction.parseFrom(byteVector2bytes(blob))
        }()

        def purejava = {
            def keyPair = Ed25519Sha3.keyPairFromBytes(privateKey, publicKey)

            return Transaction.builder(accountId, instant)
                    .createAccount(accountName, domainId, keyPair.public)
                    .transferAsset(srcAccountId, dstAccountId, assetId, description, amount)
                    .setAccountDetail(accountId, key, value)
                    .sign(keyPair)
                    .build()
        }()

        expect:
        bindings == purejava
    }
}
