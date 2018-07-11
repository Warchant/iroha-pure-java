package jp.co.soramitsu.iroha.java.commands

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.Transaction
import spock.genesis.Gen
import spock.genesis.transform.Iterations
import spock.lang.Specification

import java.time.Instant


class CreateAccountTest extends Specification {
    def ed = new Ed25519Sha3()
    def keypair = ed.generateKeypair()

    @Iterations(100)
    def "build createAccount"() {
        given:
        def time = Instant.ofEpochMilli(timestamp)

        when:
        def tx = Transaction.builder(accountId, time)
                .createAccount(accountName, domainId, keypair.getPublic())
                .sign(keypair)
                .build()

        then:
        noExceptionThrown()

        where:
        accountId << Gen.string(15)
        timestamp << Gen.long
        accountName << Gen.string(20)
        domainId << Gen.string(20)
    }
}
