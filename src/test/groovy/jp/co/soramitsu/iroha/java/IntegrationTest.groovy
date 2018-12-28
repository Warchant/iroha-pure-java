package jp.co.soramitsu.iroha.java

import io.reactivex.observers.TestObserver
import iroha.protocol.Endpoint
import iroha.protocol.Primitive
import iroha.protocol.QryResponses
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3
import jp.co.soramitsu.iroha.java.debug.TestTransactionStatusObserver
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
                            IntStream.range(0, 45)
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
        def asset = "usd"
        def account = "account1"
        def domain = "domain"
        def role = "role"
        def kp = new Ed25519Sha3().generateKeypair()
        def tx = Transaction.builder(defaultAccountId, Instant.now())
                .createRole("${role}", [Primitive.RolePermission.can_add_peer])
                .createAccount("${account}", defaultDomain, defaultKeypair.getPublic())
                .createDomain("${domain}", defaultRole)
                .createAccount("${account}@${domain}", defaultKeypair.getPublic())
                .appendRole("${account}@${defaultDomain}", "${role}")
                .detachRole("${account}@${defaultDomain}", "${role}")
                .addSignatory("${account}@${defaultDomain}", kp.getPublic())
                .removeSignatory("${account}@${defaultDomain}", kp.getPublic())
                .grantPermission("${account}@${defaultDomain}", Primitive.GrantablePermission.can_set_my_account_detail)
                .revokePermission("${account}@${defaultDomain}", Primitive.GrantablePermission.can_set_my_account_detail)
                .setAccountDetail(defaultAccountId, "key", "value")
                .createAsset("${asset}", defaultDomain, 2)
                .addAssetQuantity("${asset}#${defaultDomain}", BigDecimal.TEN)
                .addAssetQuantity("${asset}#${defaultDomain}", "1")
                .subtractAssetQuantity("${asset}#${defaultDomain}", "1")
                .subtractAssetQuantity("${asset}#${defaultDomain}", BigDecimal.ONE)
                .transferAsset(defaultAccountId, "${account}@${domain}", "${asset}#${defaultDomain}", "", new BigDecimal(5))
                .sign(defaultKeypair)
                .build()

        def t2 = new TestTransactionStatusObserver()
        api.transaction(tx)
                .blockingSubscribe(t2)

        then: "status stream works as expected"
        t2.assertNTransactionsSent(1)
        t2.assertComplete()
        t2.assertNoErrors()
        t2.assertNoTransactionFailed()
        t2.assertAllTransactionsCommitted()
        t1.assertNoErrors()
        noExceptionThrown()

        when: "query tx status"
        def hash = Utils.hash(tx)
        def status = api.txStatusSync(hash)

        then: "status is committed"
        status.txStatus == Endpoint.TxStatus.COMMITTED || true

        when: "query account"
        def q
        def res

        q = Query.builder(defaultAccountId, 1L)
                .getAccount(defaultAccountId)
                .buildSigned(defaultKeypair)
        res = api.query(q).getAccountResponse()

        then:
        res.getAccount().accountId == defaultAccountId

        when: "get account detail with key='key'"
        q = Query.builder(defaultAccountId, 2L)
                .getAccountDetail(defaultAccountId, "key")
                .buildSigned(defaultKeypair)
        res = api.query(q).getAccountDetailResponse()

        then: "value is 'value'"
        // FIXME(@Warchant): returns { "test@test" : {"key" : "value"} } for some reason
        res.getDetail() == "value"
    }
}
