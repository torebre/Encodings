package com.kjipo.raster.stochasticflow;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.AtomicDouble;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RunStochasticFlow {


    public List<StochasticFlowRasterImpl> createRun(boolean raster[][]) {
        int sourceRow = 0;
        int sourceColumn = 0;

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (raster[row][column]) {
                    sourceRow = row;
                    sourceColumn = column;
                }
            }
        }

        StochasticFlowRasterBuilder builder = StochasticFlowRasterBuilder.builder(raster.length, raster[0].length);

        TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(sourceRow, sourceColumn, raster);

        FlowDirection[] values = FlowDirection.values();

        for (int i = 0; i < values.length; ++i) {
            if (tileTypes[i] == TileType.OPEN) {
                builder.setValue(sourceRow, sourceColumn, 1, values[i]);
            }
        }


        List<StochasticFlowRasterImpl> rasterRun = new ArrayList<>();
        StochasticFlowRasterImpl firstRaster = builder.build();
        rasterRun.add(firstRaster);

        for (int i = 0; i < 10; ++i) {
            StochasticFlowRasterImpl nextRaster = iterate(firstRaster, raster);
            rasterRun.add(nextRaster);
            firstRaster = nextRaster;
        }

        return rasterRun;
    }


    private StochasticFlowRasterImpl iterate(StochasticFlowRaster stochasticFlowRaster, boolean rawData[][]) {
        StochasticFlowRasterBuilder builder = StochasticFlowRasterBuilder.builder(rawData.length, rawData[0].length);

        for (int row = 0; row < stochasticFlowRaster.getRows(); ++row) {
            for (int column = 0; column < stochasticFlowRaster.getColumns(); ++column) {
                int outputDistribution[] = new int[FlowDirection.values().length];

                for (FlowDirection flowDirection : FlowDirection.values()) {
                    int flowIntoCell = stochasticFlowRaster.getFlowIntoCell(row, column, flowDirection);
                    outputDistribution[StochasticFlowUtilities.OPPOSITE_DISTRIBUTION_MAP.get(flowDirection).ordinal()] = flowIntoCell;
                }

                TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(row, column, rawData);

                for (int i = 0; i < outputDistribution.length; ++i) {
                    switch (tileTypes[i]) {
                        case OUTSIDE_SCREEN:
                        case OUTSIDE_CHARACTER:
                            outputDistribution[i] = 0;
                            break;

                        case OPEN:
                        default:
                            // No change in probability

                    }
                }

                FlowDirection flowDirection = determineOutputDirection(outputDistribution);
                builder.setValue(row, column, 1, flowDirection);

            }

        }


        return builder.build();


    }


    private static FlowDirection determineOutputDirection(int outputDistribution[]) {
        List<Integer> outputs = Ints.asList(outputDistribution);

        int sum = IntStream.of(outputDistribution).sum();

        AtomicDouble accSum = new AtomicDouble();

        List<Double> accumulatedValues = outputs.stream()
                .map(Integer::doubleValue)
                .map(value -> value / sum)
                .map(accSum::addAndGet)
                .collect(Collectors.toList());

        double random = Math.random();

        for (int i = 1; i < accumulatedValues.size(); ++i) {
            if (accumulatedValues.get(i - 1) < random
                    && accumulatedValues.get(i) > random) {
                return FlowDirection.values()[i];
            }
        }

        return FlowDirection.values()[FlowDirection.values().length - 1];


    }


}
