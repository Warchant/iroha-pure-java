package jp.co.soramitsu.iroha.java

import io.reactivex.observers.TestObserver
import iroha.protocol.Endpoint
import iroha.protocol.Primitive
import iroha.protocol.QryResponses
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import jp.co.soramitsu.iroha.testcontainers.PeerConfig
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import spock.lang.Specification

import java.security.PublicKey
import java.time.Instant
import java.util.stream.Collectors
import java.util.stream.IntStream

class IntegrationTest extends Specification {

    final def defaultAccount = "test"
    final def defaultRole = "default"
    final def defaultDomain = "test"
    final def defaultKeypair = GenesisBlockBuilder.defaultKeyPair
    final def defaultAccountId = String.format("%s@%s", defaultAccount, defaultDomain)

    IrohaContainer iroha = new IrohaContainer()
    IrohaAPI api

    PeerConfig config = PeerConfig.builder()
            .genesisBlock(
            new GenesisBlockBuilder()
                    .addTransaction(
                    Transaction.builder((String) null, Instant.now())
                            .addPeer("0.0.0.0:10001", defaultKeypair.getPublic() as PublicKey)
                            .createRole(
                            defaultRole,
                            // all permissions
                            IntStream.range(0, 42)
                                    .boxed()
                                    .map(Primitive.RolePermission.&forNumber)
                                    .collect(Collectors.toList()) as Iterable)
                            .createDomain(defaultDomain, defaultRole)
                            .createAccount(defaultAccount, defaultDomain, defaultKeypair.getPublic())
                            .sign(defaultKeypair).build()
            ).build()
    ).build()


    def setup() {
        iroha.withPeerConfig(config)
                .start()

        api = new IrohaAPI(iroha.toriiAddress)
    }

    def cleanup() {
        iroha.stop()
    }

    def "big integration test"() {
        when: "subscribe on new blocks"
        def bq = BlocksQuery.builder(defaultAccountId, Instant.now(), 1L)
                .getQuery()
                .buildSigned(defaultKeypair)

        def t1 = new TestObserver<QryResponses.BlockQueryResponse>()
        api.blocksQuery(bq)
                .subscribe(t1)

        then: "blocks query works"
        t1.assertSubscribed()
        t1.assertNoTimeout()
        t1.assertNotComplete()
        t1.assertNoErrors()
        t1.assertEmpty()
        noExceptionThrown()

        when: "new valid transaction is sent"
        def tx = Transaction.builder(defaultAccountId, Instant.now())
                .createRole("role", [Primitive.RolePermission.can_add_peer])
                .createAccount("account1", defaultDomain, defaultKeypair.getPublic())
                .createDomain("domain", defaultRole)
                .grantPermission("account1@" + defaultDomain, Primitive.GrantablePermission.can_set_my_account_detail)
                .grantPermissions("account1@" + defaultDomain,
                [
                        Primitive.GrantablePermission.can_remove_my_signatory,
                        Primitive.GrantablePermission.can_add_my_signatory
                ])
                .setAccountDetail(defaultAccountId, "key", "value")
                .createAsset("usd", defaultDomain, 2)
                .addAssetQuantity("usd#" + defaultDomain, BigDecimal.TEN)
                .transferAsset(defaultAccountId, "account1@domain", "usd#test", "", new BigDecimal(5))
                .sign(defaultKeypair)
                .build()

        def t2 = new TestObserver<Endpoint.ToriiResponse>()
        api.transaction(tx)
                .blockingSubscribe(t2)

        then: "status stream works as expected"
        t2.assertSubscribed()
        t2.assertComplete()
        t2.assertNoErrors()
        t2.assertNoTimeout()
        t1.assertValueCount(1)
        noExceptionThrown()

        when: "query account"
        def q = Query.builder(defaultAccountId, 1L)
                .getAccount(defaultAccountId)
                .buildSigned(defaultKeypair)
        def res = api.query(q).getAccountResponse()

        then:
        res.getAccount().accountId == defaultAccountId
    }
}
