package jp.co.soramitsu.iroha.java.mapping

import jp.co.soramitsu.iroha.java.detail.mapping.AmountMapper
import jp.co.soramitsu.iroha.java.detail.mapping.Uint256Mapper
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
        "1000" | 3         | new BigDecimal("0.1")
        "15"  | 1         | new BigDecimal("-1.5")
    }
}
