package jp.co.soramitsu.iroha.java


import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.debug.TestTransactionStatusObserver
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import jp.co.soramitsu.iroha.testcontainers.PeerConfig
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import jp.co.soramitsu.iroha.testcontainers.detail.IrohaConfig
import spock.lang.Specification

class MstTest extends Specification {

    def crypto = new Ed25519Sha3()

    def keyPairA = crypto.generateKeypair()
    def keyPairB = crypto.generateKeypair()
    def mstAccountId = "mst@test"

    def getPeerConfig() {
        return PeerConfig.builder()
                .irohaConfig(getIrohaConfig())
                .genesisBlock(getGenesisBlock())
                .build()
    }

    def getGenesisBlock() {
        return new GenesisBlockBuilder()
                .addDefaultTransaction()
                .addTransaction(
                Transaction.builder(null)
                        .createAccount(mstAccountId, keyPairA.getPublic())
                        .addSignatory(mstAccountId, keyPairB.getPublic())
                        .setAccountQuorum(mstAccountId, 2)
                        .build()
                        .build())
                .build()
    }

    def getIrohaConfig() {
        return IrohaConfig.builder()
                .mst_enable(true)
                .build()
    }

    def tx() {
        def tx = Transaction.builder(mstAccountId)
                .createAsset("usd", GenesisBlockBuilder.defaultDomainName, 2)
                .build()

        return tx
    }

    def iroha = new IrohaContainer()
            .withPeerConfig(getPeerConfig())

    def api

    def setup() {
        iroha.start()
        api = iroha.getApi()
    }

    def cleanup() {
        iroha.stop()
    }

    def "two signers, same process"() {
        def obs = new TestTransactionStatusObserver()

        when:
        def t1 = tx()
                .sign(keyPairA)
                .sign(keyPairB)
                .build()

        api.transaction(t1)
                .blockingSubscribe(obs)

        then:
        noExceptionThrown()
        t1.getSignaturesCount() == 2
        obs.assertNTransactionsSent(1)
        obs.assertAllTransactionsCommitted()
        obs.assertNoTransactionFailed()
    }

    def "two signers, only one signed"() {
        given:
        def obs = new TestTransactionStatusObserver()

        when: "A signed transaction"
        def tx1 = tx().sign(keyPairA).build()

        and: "then tx is sent to iroha"
        api.transaction(tx1).blockingSubscribe(obs)

        then:
        obs.assertNTransactionsSent(1)
        obs.assertAllTransactionsFailed()
        obs.assertNoTransactionCommitted()
    }

    def "two signers, different processes"() {
        def obs = new TestTransactionStatusObserver()

        when: "A signed transaction"
        def tx1 = tx().sign(keyPairA).build()

        // send transaction from A to B

        and: "B signed transaction"
        def tx2 = Transaction.parseFrom(tx1).sign(keyPairB).build()

        and: "then tx is sent to iroha"
        api.transaction(tx2)
                .blockingSubscribe(obs)

        then:
        noExceptionThrown()
        tx2.getSignaturesCount() == 2
        obs.assertNTransactionsSent(1)
        obs.assertAllTransactionsCommitted()
        obs.assertNoTransactionFailed()
    }

    def p = { a, fail = false ->
        { b ->
            if (fail) {
                throw new RuntimeException(b as String)
            }

            println("${a}: ${b}")
        }
    }
}
