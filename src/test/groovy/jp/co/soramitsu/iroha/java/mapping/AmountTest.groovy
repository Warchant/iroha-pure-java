package jp.co.soramitsu.iroha.java.mapping

import iroha.protocol.Primitive
import jp.co.soramitsu.iroha.java.detail.mapping.AmountMapper
import jp.co.soramitsu.iroha.java.detail.mapping.Uint256Mapper
import spock.genesis.Gen
import spock.lang.Specification


class AmountTest extends Specification {

    def "domain to proto"() {
        given:
        def big = new BigInteger(value)
        def domain = new BigDecimal(big, precision)

        when:
        def proto = AmountMapper.toProtobufValue(domain)

        then:
        proto.getPrecision() == precision
        Uint256Mapper.toDomainValue(proto.getValue()) == big

        where:
        value  | precision | result
        "0"    | 0         | BigDecimal.ZERO
        "123"  | 3         | new BigDecimal("0.123")
        "1000" | 4         | new BigDecimal("0.1")
        "15"   | 1         | new BigDecimal("1.5")
    }

    def "proto to domain"() {
        given:
        def val = Uint256Mapper.toProtobufValue(new BigInteger(value))
        def proto = Primitive.Amount.newBuilder()
                .setPrecision(precision)
                .setValue(val)
                .build()

        when:
        def domain = AmountMapper.toDomainValue(proto)

        then:
        domain == expected

        where:
        value  | precision | expected
        "0"    | 0         | BigDecimal.ZERO
        "123"  | 3         | new BigDecimal("0.123")
        "1000" | 4         | new BigDecimal("0.1")
        "15"   | 1         | new BigDecimal("1.5")
    }

    def "randomized test"() {
        given:
        def domain = new BigDecimal(domainStr)

        when: 'domain -> proto -> domain'
        def proto = AmountMapper.toProtobufValue(domain)
        def actual = AmountMapper.toDomainValue(proto)

        then:
        actual == domain
        notThrown IllegalArgumentException

        where:
        domainStr << Gen.string(~/[1-9]\d+\.\d+/).take(10000)
    }

    def "string to proto" (){
        given:
        def amount = "1.37"

        when:
        def proto = AmountMapper.toProtobufValue(amount)

        println(proto)
        then:
        true

    }
}
