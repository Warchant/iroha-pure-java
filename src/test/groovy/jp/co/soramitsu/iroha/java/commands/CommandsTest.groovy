package jp.co.soramitsu.iroha.java.commands

import jp.co.soramitsu.iroha.java.Transaction
import spock.lang.Specification

import static javax.xml.bind.DatatypeConverter.printHexBinary

class CommandsTest extends Specification {

    def "integration test [transfer asset]"() {
        given:
        def txhash = "A338EBF5EBABAB148E4DEB328F56FF94D2AE621D9D405BF5F1EA1D1758EE9CC5"
        def txStr = "payload {\n" +
                "  commands {\n" +
                "    transfer_asset {\n" +
                "      src_account_id: \"alice_green_2018@nbc\"\n" +
                "      dest_account_id: \"bob_big_2018@nbc\"\n" +
                "      asset_id: \"usd#nbc\"\n" +
                "      description: \".\"\n" +
                "      amount {\n" +
                "        value {\n" +
                "          fourth: 1\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  creator_account_id: \"alice_green_2018@nbc\"\n" +
                "  created_time: 1541232209000\n" +
                "  quorum: 1\n" +
                "}\n"

        when:
        def tx = Transaction.builder("alice_green_2018@nbc", 1541232209000)
                .transferAsset(
                "alice_green_2018@nbc",
                "bob_big_2018@nbc",
                "usd#nbc",
                ".",
                "1"
        )
                .build()

        def actualTxStr = tx.build().toString()
        def actualTxHash = printHexBinary(tx.hash())

        then:
        actualTxStr == txStr
        actualTxHash == txhash

    }

}
