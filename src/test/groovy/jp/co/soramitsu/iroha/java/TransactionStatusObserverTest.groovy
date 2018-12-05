package jp.co.soramitsu.iroha.java

import io.reactivex.Observable
import iroha.protocol.Endpoint
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import spock.lang.Specification


class TransactionStatusObserverTest extends Specification {

    def iroha = new IrohaContainer()
            .withLogger(null)

    def setup() {
        iroha.start()
    }

    def cleanup() {
        iroha.stop()
    }

    def "onError handler is executed for exceptions thrown from handlers"() {
        given:
        def caught = false
        def msg = "1337"
        def observer = TransactionStatusObserver.builder()
                .onComplete({ throw new Exception(msg) })
                .onError({ e ->
            caught = true
            assert e.message == msg
        }).build()

        when:
        Observable.just(new Endpoint.ToriiResponse())
                .blockingSubscribe(observer)

        then:
        caught
    }
}
