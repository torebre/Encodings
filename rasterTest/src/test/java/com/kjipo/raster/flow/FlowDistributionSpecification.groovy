package com.kjipo.raster.flow

import spock.lang.Specification
import static spock.util.matcher.HamcrestSupport.*
import static spock.util.matcher.HamcrestMatchers.*


class FlowDistributionSpecification extends Specification {

    def "Flow distributed as expected test case 1"() {
        given:
        def values = FlowDistribution.distributeFlow(1.0D, Math.PI / 2)

        expect:
        values.length == 8
        // TODO Why is the sum not closer to 1?
        that values.sum(), closeTo(1.0D, 0.01D)
    }

    def "Flow distributed as expected test case 2"() {
        given:
        def values = FlowDistribution.distributeFlow(1.0D, 1.0D)

        expect:
        values.length == 8
        // TODO Why is the sum not closer to 1?
        that values.sum(), closeTo(1.0D, 0.01D)
    }

    def "Integral test cases"() {
        expect:
        that FlowDistribution.computeIntegralForTriangularDissipationFunction(Math.PI / 2, 0D, Math.PI, FlowDistribution.TRIANGLE_BASE), closeTo(0.75D, 0.001D)
        that FlowDistribution.computeIntegralForTriangularDissipationFunction(0D, Math.PI / 2, Math.PI, FlowDistribution.TRIANGLE_BASE), closeTo(0.125D, 0.001D)
        that FlowDistribution.computeIntegralForTriangularDissipationFunction(0D, -Math.PI, Math.PI, FlowDistribution.TRIANGLE_BASE), closeTo(1D, 0.001D)
        that FlowDistribution.computeIntegralForTriangularDissipationFunction(0D, -Math.PI, Math.PI, Math.PI / 2), closeTo(1D, 0.001D)

        FlowDistribution.computeIntegralForTriangularDissipationFunction(7.283185307179586D, 6.675884388878311D, 7.461282552275758D, FlowDistribution.TRIANGLE_BASE) > 0
    }

}
