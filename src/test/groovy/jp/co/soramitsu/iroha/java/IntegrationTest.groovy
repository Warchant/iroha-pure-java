package jp.co.soramitsu.iroha.java

import io.reactivex.observers.TestObserver
import iroha.protocol.Endpoint
import iroha.protocol.Primitive
import iroha.protocol.QryResponses
import iroha.protocol.TransactionOuterClass.Transaction.Payload.BatchMeta.BatchType
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
        def q
        def res

        q = Query.builder(defaultAccountId, 1L)
                .getAccount(defaultAccountId)
                .buildSigned(defaultKeypair)
        res = api.query(q).getAccountResponse()

        then:
        res.getAccount().accountId == defaultAccountId

        when: "transactions batch is created and sent to iroha"
        def anotherAccount = "anotheraccount"
        def anotherAccountId = "${anotherAccount}@${defaultDomain}"
        def batchPrepare = [
                Transaction.builder(defaultAccountId, Instant.now())
                        .createAccount("${anotherAccount}", defaultDomain, defaultKeypair.getPublic()),
                Transaction.builder(defaultAccountId, Instant.now())
                        .appendRole(anotherAccountId, "${role}"),
                Transaction.builder(defaultAccountId, Instant.now())
                        .setAccountDetail(anotherAccountId, "key", "value")
        ]
        def batchReducedHashes = batchPrepare
                .stream()
                .map({ batchTxBuilder -> batchTxBuilder.build() })
                .map({ batchTx -> ((Transaction) batchTx).getReducedHashHex() })
                .collect(Collectors.toList())
        def batch = batchPrepare
                .stream()
                .map({ batchTxBuilder ->
            batchTxBuilder.setBatchMeta(BatchType.ORDERED, batchReducedHashes)
            batchTxBuilder.sign(defaultKeypair).build()
        })
                .collect(Collectors.toList())

        api.transactionListSync(batch)
        Thread.sleep(2000)
        
        then: "transaction result was committed"
        def queryResponse = api.query(
                Query.builder(defaultAccountId, 1)
                        .getAccount(anotherAccountId)
                        .buildSigned(defaultKeypair)
        ).getAccountResponse()

        then: "account is created, role is appended and details are set"
        queryResponse.account.accountId == anotherAccountId
        queryResponse.accountRolesCount == 2
        queryResponse.getAccount().jsonData == "{\"test@test\": {\"key\": \"value\"}}"
    }
}
