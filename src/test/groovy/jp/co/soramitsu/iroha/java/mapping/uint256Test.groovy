package jp.co.soramitsu.iroha.java.mapping

import iroha.protocol.Primitive
import jp.co.soramitsu.iroha.java.detail.mapping.Uint256Mapper
import spock.genesis.Gen
import spock.lang.Specification
import spock.lang.Unroll

class uint256Test extends Specification {

    @Unroll
    def "proto (#a, #b, #c, #d) to domain"() {
        given:
        def proto = Primitive.uint256
                .newBuilder()
                .setFirst(a)
                .setSecond(b)
                .setThird(c)
                .setFourth(d)
                .build()

        when:
        def domain = Uint256Mapper.toDomainValue(proto)

        then:
        domain == expected

        where:
        a | b | c | d | expected
        0 | 0 | 0 | 0 | BigInteger.ZERO
        0 | 0 | 0 | 1 | BigInteger.ONE
        0 | 0 | 0 | 2 | new BigInteger("2")
        0 | 0 | 3 | 2 | new BigInteger("55340232221128654850")
        0 | 4 | 3 | 2 | new BigInteger("1361129467683753853908838661948201500674")
        5 | 4 | 3 | 2 | new BigInteger("31385508676933403820540076583722085934420615884268374065154")
    }

    @Unroll
    def "domain to proto (#a, #b, #c, #d)"() {
        when:
        def proto = Uint256Mapper.toProtobufValue(domain)

        then:
        proto == Primitive.uint256
                .newBuilder()
                .setFirst(a)
                .setSecond(b)
                .setThird(c)
                .setFourth(d)
                .build()

        where:
        a | b | c | d | domain
        0 | 0 | 0 | 0 | BigInteger.ZERO
        0 | 0 | 0 | 1 | BigInteger.ONE
        0 | 0 | 0 | 2 | new BigInteger("2")
        0 | 0 | 3 | 2 | new BigInteger("55340232221128654850")
        0 | 4 | 3 | 2 | new BigInteger("1361129467683753853908838661948201500674")
        5 | 4 | 3 | 2 | new BigInteger("31385508676933403820540076583722085934420615884268374065154")
    }

    def "randomized test"() {
        given:
        def domain = new BigInteger(domainStr)

        when: 'domain -> proto -> domain'
        def proto = Uint256Mapper.toProtobufValue(domain)
        def actual = Uint256Mapper.toDomainValue(proto)

        then:
        actual == domain
        notThrown IllegalArgumentException

        where:
        domainStr << Gen.string(~/[1-9][0-9]{55,75}/).take(10000)
    }

    def "uint256 overflow"() {
        given: 'value bigger than 2**256'
        def domain = new BigInteger("11111111111111111111111111111111111111111111111111111111111111111111111111111111")

        when: 'domain -> proto -> domain'
        def proto = Uint256Mapper.toProtobufValue(domain)
        def actual = Uint256Mapper.toDomainValue(proto)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'BigInteger does not fit into uint256'
    }

    def "uint256 underflow (negative)"() {
        given:
        def neg = new BigInteger("-1")

        when: 'domain -> proto -> domain'
        def proto = Uint256Mapper.toProtobufValue(neg)
        def actual = Uint256Mapper.toDomainValue(proto)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'BigInteger must be positive'
    }

}
