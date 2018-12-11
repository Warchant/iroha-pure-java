package jp.co.soramitsu.iroha.java


import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.IntStream

import static jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder.*

class IrohaAPITest extends Specification {

    private IrohaContainer iroha = new IrohaContainer()

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

    def "send transaction list"() {
        given:
        def api = iroha.getApi()
        def txs = IntStream.range(0, 100)
                .boxed()
                .map(String.&valueOf)
                .map(
                { String name ->
                    return Transaction.builder(defaultAccountId)
                            .createAccount(name, defaultDomainName, defaultKeyPair.getPublic())
                            .sign(defaultKeyPair)
                            .build()
                })
                .collect(Collectors.toList())

        when:
        api.transactionListSync(txs)

        then:
        txs.stream()
                .map(Utils.&hash)
                .map(
                { byte[] h ->
                    boolean onCommitted = false

                    def obs = TransactionStatusObserver.builder()
                            .onTransactionCommited({ z -> onCommitted = true })
                            .build()

                    api.txStatus(h).blockingSubscribe(obs)

                    return onCommitted
                })
                .allMatch({ p -> p })

    }
}
