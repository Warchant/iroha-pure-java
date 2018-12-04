package jp.co.soramitsu.iroha.java


import io.reactivex.observers.TestObserver
import iroha.protocol.Endpoint
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import spock.lang.Specification

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
        def obs = new TestObserver<Endpoint.ToriiResponse>()
        def api = iroha.getApi()

        when: "send valid transaction"
        def valid = Transaction.builder(defaultAccountId)
                .createAccount("z", defaultDomainName, defaultKeyPair.getPublic())
                .sign(defaultKeyPair)
                .build()

        api.transaction(valid)
                .doOnNext({ c -> println("VALUE: " + c) })
                .doOnError({ e -> println("ERROR: " + e.toString()) })
                .blockingSubscribe(obs)

        then: "observable is completed, no errors, and it received all valid statuses"
        obs.assertComplete()
        obs.assertNoErrors()
        obs.assertNoTimeout()
        obs.assertValueCount(4) // TODO: should be 3; fixed in beta-5+
    }

    def "when sending stateless invalid tx, error is reported"() {
        given:
        def obs = new TestObserver<Endpoint.ToriiResponse>()
        def api = iroha.getApi()

        when: "send stateless invalid transaction"
        // invalid account name in create account
        def statelessInvalid = Transaction.builder(defaultAccountId)
                .disableValidation()
                .createAccount("...", defaultDomainName, defaultKeyPair.getPublic())
                .sign(defaultKeyPair)
                .build()

        api.transaction(statelessInvalid)
                .doOnNext({ c -> println("VALUE: " + c) })
                .doOnError({ e -> println("ERROR: " + e.toString()) })
                .blockingSubscribe(obs)

        then: "on next returned stateless invalid, onError is called"
        obs.assertComplete()
        obs.assertValueCount(1)
        obs.assertNoErrors()
        obs.assertNoTimeout()
    }

    def "when sending stateful invalid tx, error is reported"() {
        given:
        def obs = new TestObserver<Endpoint.ToriiResponse>()
        def api = iroha.getApi()

        when: "send stateful invalid transaction"
        // unknown creator
        def statefulInvalid = Transaction.builder("random@account")
                .createAccount("x", defaultDomainName, defaultKeyPair.getPublic())
                .sign(defaultKeyPair)
                .build()

        api.transaction(statefulInvalid)
                .doOnNext({ c -> println("VALUE: " + c) })
                .doOnError({ e -> println("ERROR: " + e.toString()) })
                .blockingSubscribe(obs)

        then: "on next returned stateful invalid"
        obs.assertComplete()
        obs.assertValueCount(2)
        obs.assertNoErrors()
        obs.assertNoTimeout()
    }
}
