package jp.co.soramitsu.iroha.java.queries

import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import iroha.protocol.CommandServiceGrpc
import iroha.protocol.Endpoint
import iroha.protocol.Primitive
import jp.co.soramitsu.iroha.java.Transaction
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer
import jp.co.soramitsu.iroha.testcontainers.PeerConfig
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import org.junit.Rule
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

    @Rule
    IrohaContainer iroha = new IrohaContainer()
    ManagedChannel channel
    CommandServiceGrpc.CommandServiceBlockingStub stub

    PeerConfig config = PeerConfig.builder()
            .genesisBlock(
            new GenesisBlockBuilder()
                    .addTransaction(
                    Transaction.builder((String) null, Instant.now())
                            .addPeer("0.0.0.0:10001", defaultKeypair.getPublic() as PublicKey)
                            .createRole(
                            defaultRole,
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

        URI addr = iroha.getToriiAddress()

        channel = ManagedChannelBuilder
                .forAddress(addr.getHost(), addr.getPort())
                .usePlaintext()
                .build()

        stub = CommandServiceGrpc.newBlockingStub(channel)

    }

    def cleanup() {
        iroha.stop()
    }

    def "commands work"() {
        given:
        Transaction tx = Transaction.builder(defaultAccountId, Instant.now())
                .createRole("role", [Primitive.RolePermission.can_add_peer])
                .createAccount("account1", defaultDomain, defaultKeypair.getPublic())
                .createDomain("domain", defaultRole)
                .grantPermission("account1@" + defaultDomain, Primitive.GrantablePermission.can_set_my_account_detail)
                .setAccountDetail(defaultAccountId, "key", "value")
                .createAsset("usd", defaultDomain, 2)
                .addAssetQuantity("usd#" + defaultDomain, BigDecimal.TEN)
                .build()
                .sign(defaultKeypair) as Transaction

        byte[] hash = tx.hash()

        when:
        stub.torii(tx.build())
        waitForCommit()

        def request = Endpoint.TxStatusRequest.newBuilder()
                .setTxHash(ByteString.copyFrom(hash))
                .build()

        def response = stub.status(request)

        then: "stateful validation is passed and tx is committed"
        response.txStatus == Endpoint.TxStatus.COMMITTED


    }

    def waitForCommit() {
        sleep(100 + iroha.conf.irohaConfig.proposal_delay, {
            println("waitForCommit: interrupted")
        })
    }
}
