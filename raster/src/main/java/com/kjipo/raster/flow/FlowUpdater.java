package com.kjipo.raster.flow;


import com.kjipo.representation.raster.FlowDirection;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.raster.*;

import java.util.Queue;


public class FlowUpdater {

    private static final double KEEP_FACTOR = 0.6;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowUpdater.class);


    public static BooleanEncoding iterate(BooleanEncoding booleanEncoding) {
        return iterate(booleanEncoding, false,
                FlowDistribution.createTriangularDissipationFunction(FlowDistribution.TRIANGLE_BASE));
    }

    public static BooleanEncoding iterate(BooleanEncoding booleanEncoding, boolean createCopy,
                                          DissipationFunction dissipationFunction) {
        return createCopy ? updateFlow(new BooleanEncoding(booleanEncoding), dissipationFunction)
                : updateFlow(booleanEncoding, dissipationFunction);
    }

    public static BooleanEncoding updateFlow(BooleanEncoding booleanEncoding, DissipationFunction dissipationFunction) {
        Queue<Point> queue = BooleanEncodingUtilities.getPointsWithFlow(booleanEncoding);

        LOGGER.info("Points with flow: {}", queue.size());

        Complex updatedFlowRaster[][] = new Complex[booleanEncoding.flowRaster.length][booleanEncoding.flowRaster[0].length];
        for(int row = 0; row < updatedFlowRaster.length; ++row) {
            for(int column = 0; column < updatedFlowRaster[0].length; ++column) {
                updatedFlowRaster[row][column] = new Complex(0.0);
            }
        }

        for (Point point : queue) {
            Complex flow = booleanEncoding.flowRaster[point.getRow()][point.getColumn()];

            double angle = flow.getArgument();
            if(angle < 0) {
                angle += 2 * Math.PI;
            }

            double currentFlow = flow.abs();
            double flows[] = FlowDistribution.distributeFlow(currentFlow * (1 - KEEP_FACTOR), angle,
                    dissipationFunction);

            FlowDistribution.applyObstructions(flows, EncodingUtilities.determineNeighbourTypes(point.getRow(),
                    point.getColumn(), booleanEncoding.raster));

            int counter = -1;
            for(FlowDirection flowDirection : FlowDirection.values()) {
                ++counter;
                int rowShift = point.getRow() + flowDirection.getRowShift();
                if(rowShift < 0 || rowShift >= updatedFlowRaster.length) {
                    continue;
                }
                int columnShift = point.getColumn() + flowDirection.getColumnShift();
                if(columnShift < 0 || columnShift >= updatedFlowRaster[0].length) {
                    continue;
                }
                updatedFlowRaster[rowShift][columnShift] = updatedFlowRaster[rowShift][columnShift]
                        .add(ComplexUtils.polar2Complex(flows[counter], flowDirection.getAngleInRadians()));
            }

            updatedFlowRaster[point.getRow()][point.getColumn()] = updatedFlowRaster[point.getRow()][point.getColumn()]
                    .add(ComplexUtils.polar2Complex(currentFlow * KEEP_FACTOR, angle));
        }

//        for (Source source : booleanEncoding.sources) {
//            Complex flow = source.getFlow();
//            updatedFlowRaster[source.getRow()][source.getColumn()] = updatedFlowRaster[source.getRow()][source.getColumn()].add(flow);
//        }
        booleanEncoding.flowRaster = updatedFlowRaster;
        return booleanEncoding;
    }

}
