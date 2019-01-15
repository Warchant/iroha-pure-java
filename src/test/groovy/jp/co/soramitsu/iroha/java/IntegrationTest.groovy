package jp.co.soramitsu.iroha.java

import io.reactivex.observers.TestObserver
import iroha.protocol.Endpoint
import iroha.protocol.Primitive
import iroha.protocol.QryResponses
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

    static final def defaultAccount = "test"
    static final def defaultRole = "default"
    static final def defaultDomain = "test"
    static final def defaultKeypair = GenesisBlockBuilder.defaultKeyPair
    static final def defaultAccountId = String.format("%s@%s", defaultAccount, defaultDomain)

    static IrohaContainer iroha = new IrohaContainer()

    static IrohaAPI api

    static PeerConfig config = PeerConfig.builder()
            .genesisBlock(
            new GenesisBlockBuilder()
                    .addTransaction(Transaction.builder((String) null, Instant.now())
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


    def setupSpec() {
        iroha.withPeerConfig(config)
                .start()
        api = iroha.getApi()
    }

    def cleanupSpec() {
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
        def tx = Transaction.builder(defaultAccountId, Instant.now())
                .createRole("${role}", [Primitive.RolePermission.can_add_peer])
                .createAccount("${account}", defaultDomain, defaultKeypair.getPublic())
                .createDomain("${domain}", defaultRole)
                .createAccount("${account}@${domain}", defaultKeypair.getPublic())
                .appendRole("${account}@${defaultDomain}", "${role}")
                .detachRole("${account}@${defaultDomain}", "${role}")
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
        status.txStatus == Endpoint.TxStatus.COMMITTED

        when: "query account"
        def qapi = new QueryAPI(api, defaultAccountId, defaultKeypair)
        def res = qapi.getAccount(defaultAccountId)

        then:
        res.getAccount().accountId == defaultAccountId

        when: "transactions atomic batch is created and sent to iroha"
        def anotherAccount = "a"
        def anotherAccountId = "${anotherAccount}@${defaultDomain}"
        def atomicBatch = [
                Transaction.builder(defaultAccountId, Instant.now())
                        .createAccount(anotherAccount, defaultDomain, defaultKeypair.getPublic())
                        .sign(defaultKeypair)
                        .build(),
                Transaction.builder(defaultAccountId, Instant.now())
                        .appendRole(anotherAccountId, role)
                        .sign(defaultKeypair)
                        .build(),
                Transaction.builder(defaultAccountId, Instant.now())
                        .setAccountDetail(anotherAccountId, "key", "value1")
                        .sign(defaultKeypair)
                        .build()
        ]

        def trueBatch = Utils.createTxAtomicBatch(atomicBatch, defaultKeypair)
        api.transactionListSync(trueBatch)
        Thread.sleep(4000)

        then: "transaction result was committed"
        def atomicResponse = qapi.getAccount(anotherAccountId)

        then: "account is created, role is appended and details are set"
        atomicResponse.account.accountId == anotherAccountId
        atomicResponse.accountRolesCount == 2
        atomicResponse.getAccount().jsonData == "{\"test@test\": {\"key\": \"value1\"}}"

        when: "transactions ordered batch is created and sent to iroha"
        anotherAccount = "b"
        anotherAccountId = "${anotherAccount}@${defaultDomain}"
        def orderedBatch = [
                Transaction.builder(defaultAccountId, Instant.now())
                        .createAccount(anotherAccount, defaultDomain, defaultKeypair.getPublic())
                        .sign(defaultKeypair)
                        .build(),
                // invalid tx, intentionally
                Transaction.builder(defaultAccountId, Instant.now())
                        .appendRole(anotherAccountId, "unknownrole")
                        .build().build(),
                Transaction.builder(defaultAccountId, Instant.now())
                        .setAccountDetail(anotherAccountId, "key", "value2")
                        .sign(defaultKeypair)
                        .build()
        ]

        trueBatch = Utils.createTxOrderedBatch(orderedBatch, defaultKeypair)
        api.transactionListSync(trueBatch)
        Thread.sleep(4000)

        then: "transaction result was committed partially"
        def orderedResponse = qapi.getAccount(anotherAccountId)

        then: "account is created, role is NOT appended and details are set"
        orderedResponse.account.accountId == anotherAccountId
        orderedResponse.accountRolesCount == 1 // 2nd tx is invalid, batch processed
        orderedResponse.getAccount().jsonData == "{\"test@test\": {\"key\": \"value2\"}}"
    }
}
