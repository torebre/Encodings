package com.kjipo.raster.flow;


import com.kjipo.raster.DissipationFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.visualization.RasterVisualizer;

import java.util.ArrayList;
import java.util.List;


public class BooleanEncodingExperiments {

    private static final Logger LOGGER = LoggerFactory.getLogger(BooleanEncodingExperiments.class);


    private static void runExperiment() throws InterruptedException {
        boolean raster[][] = BooleanEncodingTestData.getTestRaster4();
        BooleanEncoding encoding = new BooleanEncoding(raster, BooleanEncodingUtilities.setupFlowRaster(raster));

        DissipationFunction dissipationFunction = FlowDistribution.createTriangularDissipationFunction(Math.PI / 2);

        BooleanEncodingIterator.placeSource(encoding);

        doAndSaveRun(encoding, dissipationFunction);
    }

    private static void runExperiment2() throws InterruptedException {
        boolean raster[][] = BooleanEncodingTestData.getTestRaster5();
        BooleanEncoding encoding = new BooleanEncoding(raster, BooleanEncodingUtilities.setupFlowRaster(raster));

        DissipationFunction dissipationFunction = FlowDistribution.createTriangularDissipationFunction(Math.PI / 2);

        BooleanEncodingIterator.placeSource(encoding);

        doAndSaveRun(encoding, dissipationFunction);
    }

    private static void doAndSaveRun(BooleanEncoding encoding, DissipationFunction dissipationFunction)
            throws InterruptedException {
        List<BooleanEncoding> run = new ArrayList<>();
        run.add(encoding);

        double flowIncreaseFactor = 2;
        double flowIncrease = 1;

        for (int i = 1; i < 1000; ++i) {
            BooleanEncoding updateRun = new BooleanEncoding(run.get(i - 1));
//            if(i % 5 == 0) {
            for (Source source : encoding.sources) {
                Complex flow = source.getFlow();
                double flowNumber = flow.abs();

                if (i % 5 == 0) {
                    flowIncrease += flowIncreaseFactor;
                    flowNumber += flowIncrease;
                }

                LOGGER.info("Flow before update: {}",
                        updateRun.flowRaster[source.getRow()][source.getColumn()].abs());

                updateRun.flowRaster[source.getRow()][source.getColumn()] =
                        updateRun.flowRaster[source.getRow()][source.getColumn()]
                                .add(ComplexUtils.polar2Complex(flowNumber,
//                                            flow.getArgument()
                                        updateRun.flowRaster[source.getRow()][source.getColumn()].getArgument()
                                ));

                LOGGER.info("Updating flow at {}, {}: {}", source.getRow(), source.getColumn(),
                        updateRun.flowRaster[source.getRow()][source.getColumn()].abs());
            }
//            }

//            run.add(BooleanEncodingIterator.iterate(run.get(i - 1), true));
            run.add(FlowUpdater.iterate(updateRun, false, dissipationFunction));

        }
        RasterVisualizer.showRasterFlow(run, encoding.raster);
    }


    public static void main(String args[]) throws Exception {
//        doAndSaveRun();

        runExperiment2();

    }


}
