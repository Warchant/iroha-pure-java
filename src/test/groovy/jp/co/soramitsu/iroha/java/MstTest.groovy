package jp.co.soramitsu.iroha.java

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
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
                        .build()
                        .build())
                .addTransaction(
                Transaction.builder(mstAccountId)
                        .setQuorum(2)
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
            .withLogger(null)

    def setup() {
        iroha.start()
    }

    def cleanup() {
        iroha.stop()
    }

    def "two signers, same process"() {
        given:
        IrohaAPI api = iroha.getApi()

        def observer = TransactionStatusObserver.builder()
                .onError(p("error", true))
                .onTransactionFailed(p("failed", true))
                .onTransactionCommited(p("committed"))
                .onStatelessValidationSuccess(p("stateless success"))
                .onStatefulValidationSuccess(p("stateful success"))
                .onMstFailed(p("mst failed", true))
                .onNotReceived(p("not received", true))
                .onUnrecognizedStatus(p("unknown status", true))
                .build()
        when:
        api.transaction(tx()
                .sign(keyPairA)
                .sign(keyPairB)
                .build()
        )
                .blockingSubscribe(observer)

        then:
        noExceptionThrown()
    }

    def "two signers, different processes"() {
        given:
        IrohaAPI api = iroha.getApi()

        def observer = TransactionStatusObserver.builder()
                .onError(p("error", true))
                .onTransactionFailed(p("failed", true))
                .onTransactionCommited(p("committed"))
                .onStatelessValidationSuccess(p("stateless success"))
                .onStatefulValidationSuccess(p("stateful success"))
                .onMstFailed(p("mst failed", true))
                .onNotReceived(p("not received", true))
                .onUnrecognizedStatus(p("unknown status", true))
                .build()

        when: "A signed transaction"
        def tx1 = tx().sign(keyPairA).build()

        // send transaction from A to B

        and: "B signed transaction"
        def tx2 = Transaction.parseFromProto(tx1).sign(keyPairB).build()

        and: "then tx is sent to iroha"
        api.transaction(tx2)
                .blockingSubscribe(observer)

        then:
        noExceptionThrown()
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
