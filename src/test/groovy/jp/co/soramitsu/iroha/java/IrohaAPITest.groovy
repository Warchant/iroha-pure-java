package jp.co.soramitsu.iroha.java


import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import spock.lang.Specification

import static jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder.*

class IrohaAPITest extends Specification {

    private IrohaContainer iroha = new IrohaContainer()
            .withLogger(null /* disable logger */)

    def setup() {
        iroha.start()
    }

    def cleanup() {
        iroha.stop()
    }

    def "valid transaction is accepted"() {
        given:
        def api = iroha.getApi()
        def failed = false
        def committed = false

        when: "send valid transaction"
        def valid = Transaction.builder(defaultAccountId)
                .createAccount("z", defaultDomainName, defaultKeyPair.getPublic())
                .sign(defaultKeyPair)
                .build()

        def subscriber = TransactionStatusObserver.builder()
                .onTransactionFailed({ failed = true })
                .onTransactionCommited({ committed = true })
                .build()

        def observable = api.transaction(valid)
        observable.blockingSubscribe(subscriber)

        then:
        noExceptionThrown()
        !failed
        committed
    }

    def "when sending stateless invalid tx, error is reported"() {
        given:
        def api = iroha.getApi()
        def failed = false
        def committed = false

        when: "send stateless invalid transaction"
        // invalid account name in create account
        def statelessInvalid = Transaction.builder(defaultAccountId)
                .disableValidation()
                .createAccount("...", defaultDomainName, defaultKeyPair.getPublic())
                .sign(defaultKeyPair)
                .build()

        def subscriber = TransactionStatusObserver.builder()
                .onTransactionFailed({ failed = true })
                .onTransactionCommited({ committed = true })
                .build()

        def observable = api.transaction(statelessInvalid)
        observable.blockingSubscribe(subscriber)

        then:
        noExceptionThrown()
        failed
        !committed
    }

    def "when sending stateful invalid tx, error is reported"() {
        given:
        def api = iroha.getApi()
        def failed = false
        def committed = false

        when: "send stateful invalid transaction"
        // unknown creator
        def statefulInvalid = Transaction.builder("random@account")
                .createAccount("x", defaultDomainName, defaultKeyPair.getPublic())
                .sign(defaultKeyPair)
                .build()

        def subscriber = TransactionStatusObserver.builder()
                .onTransactionFailed({ failed = true })
                .onTransactionCommited({ committed = true })
                .build()

        def observable = api.transaction(statefulInvalid)
        observable.blockingSubscribe(subscriber)

        then:
        noExceptionThrown()
        failed
        !committed
    }
}
