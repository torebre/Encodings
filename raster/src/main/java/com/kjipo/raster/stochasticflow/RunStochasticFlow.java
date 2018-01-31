package com.kjipo.raster.stochasticflow;

import com.kjipo.raster.AbstractCell;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.TileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RunStochasticFlow {

    private static final Logger LOG = LoggerFactory.getLogger(RunStochasticFlow.class);


    public List<StochasticFlowRasterImpl> createRun(boolean raster[][]) {
        int sourceRow = 0;
        int sourceColumn = 0;

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (raster[row][column]) {
                    sourceRow = row;
                    sourceColumn = column;
                    break;
                }
            }
        }

        StochasticFlowRasterBuilder builder = StochasticFlowRasterBuilder.builder(raster.length, raster[0].length);

        TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(sourceRow, sourceColumn, raster);

        LOG.info("Setting source at {}, {}", sourceRow, sourceColumn);


        FlowDirection[] values = FlowDirection.values();

        for (int i = 0; i < values.length; ++i) {
            if (tileTypes[i] == TileType.OPEN) {
//                builder.setValue(sourceRow, sourceColumn, 1, values[i]);

                builder.addSource(sourceRow, sourceColumn, values[i]);
                builder.addCellsToProcessNext(Collections.singleton(new BaseCell(sourceRow + values[i].getRowShift(), sourceColumn + values[i].getColumnShift())));
            }
        }


        List<StochasticFlowRasterImpl> rasterRun = new ArrayList<>();
        StochasticFlowRasterImpl firstRaster = builder.build();
        rasterRun.add(firstRaster);

        for (int i = 0; i < 100; ++i) {
            StochasticFlowRasterImpl nextRaster = iterate(firstRaster, raster);

            printNumberOfCellsWithFlow(nextRaster);

            rasterRun.add(nextRaster);

            if (nextRaster.getCellsToProcessNext().isEmpty()) {
                break;
            }

            firstRaster = nextRaster;
        }

        return rasterRun;
    }

    private static void printNumberOfCellsWithFlow(StochasticFlowRaster stochasticFlowRaster) {
        int cellsWithFlow = 0;
        for (int row = 0; row < stochasticFlowRaster.getRows(); ++row) {
            for (int column = 0; column < stochasticFlowRaster.getColumns(); ++column) {
                if (stochasticFlowRaster.getFlowInCell(row, column) > 0) {
                    ++cellsWithFlow;
                }
            }
        }

        LOG.info("Cells with flow: {}", cellsWithFlow);
    }


    private StochasticFlowRasterImpl iterate(StochasticFlowRaster stochasticFlowRaster, boolean rawData[][]) {
        StochasticFlowRasterBuilder builder = StochasticFlowRasterBuilder.builder(rawData.length, rawData[0].length);
        // Copy sources
        stochasticFlowRaster.getSources().forEach(builder::addSource);
        builder.addProcessedCells(stochasticFlowRaster.getProcessedCells());
        builder.addProcessedCells(stochasticFlowRaster.getCellsToProcessNext());

        for (Source source : stochasticFlowRaster.getSources()) {
            builder.setValue(source.getRow(), source.getColumn(), 1, source.getFlowDirection());
        }

        for (AbstractCell stochasticCell : stochasticFlowRaster.getCellsToProcessNext()) {
            int outputDistribution[] = new int[FlowDirection.values().length];
            int row = stochasticCell.getRow();
            int column = stochasticCell.getColumn();

            if (stochasticFlowRaster.getFlowInCell(row, column) != 0) {
                // Already a flow here
                continue;
            }

            int totalFlow = 0;
            for (FlowDirection flowDirection : FlowDirection.values()) {
                int flowIntoCell = stochasticFlowRaster.getFlowIntoCell(row, column, flowDirection);

                if (flowIntoCell == 0) {
                    continue;
                }

                totalFlow += flowIntoCell;

                if (flowIntoCell > 0) {
                    LOG.info("Found flow into cell {}, {}. Flow: {}", row, column, flowIntoCell);
                }
                outputDistribution[flowDirection.ordinal()] = flowIntoCell;
            }

            if (totalFlow == 0) {
                // No flow into this cell
                continue;
            }

            TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(row, column, rawData);
            Map<FlowDirection, Integer> outputFlows = new HashMap<>();

            for (int i = 0; i < outputDistribution.length; ++i) {
                if (outputDistribution[i] == 0) {
                    continue;
                }

                switch (tileTypes[i]) {
                    case OPEN:
                        builder.addCellsToProcessNext(
                                Collections.singleton(new BaseCell(row + FlowDirection.values()[i].getRowShift(),
                                        column + FlowDirection.values()[i].getColumnShift())));
                        break;

                    case OUTSIDE_SCREEN:
                    case OUTSIDE_CHARACTER:
                    default:
                        // Do nothing

                }
            }


            System.out.println("Output flows: " + outputFlows);

            List<FlowDirection> probabilityHelper = outputFlows.entrySet().stream().map(entry -> {
                List<FlowDirection> result = new ArrayList<>(entry.getValue());
                for (int i = 0; i < entry.getValue(); ++i) {
                    result.add(entry.getKey());
                }
                return result;
            })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            Random random = new Random();
            builder.setValue(row, column, 1, probabilityHelper.get(random.nextInt(probabilityHelper.size())));
        }

        return builder.build();
    }


    private static FlowDirection determineSingleFlow(FlowDirection flowDirection, TileType neighbours[]) {
        if (neighbours[flowDirection.ordinal()] == TileType.OPEN) {
            return flowDirection;
        }

        int clockwise = clockwiseTurn(flowDirection.ordinal(), FlowDirection.values().length);
        int counterClockwise = counterClockwiseTurn(flowDirection.ordinal(), FlowDirection.values().length);

        boolean clockwiseProbability = neighbours[clockwise] != TileType.OUTSIDE_CHARACTER
                && neighbours[clockwise] != TileType.OUTSIDE_SCREEN;

        boolean counterClockwiseProbability = neighbours[clockwise] != TileType.OUTSIDE_CHARACTER
                && neighbours[clockwise] != TileType.OUTSIDE_SCREEN;


        if (clockwiseProbability && counterClockwiseProbability || !clockwiseProbability && !counterClockwiseProbability) {
            return determineSingleFlow(FlowDirection.values()[Math.random() > 0.5 ? clockwise : counterClockwise], neighbours);
        } else if (counterClockwiseProbability) {
            return FlowDirection.values()[counterClockwise];
        } else {
            return FlowDirection.values()[clockwise];
        }
    }

    private static int clockwiseTurn(int position, int arrayLength) {
        ++position;
        if (position >= arrayLength) {
            return 0;
        }
        return position;
    }

    private static int counterClockwiseTurn(int position, int arrayLength) {
        --position;
        if (position < 0) {
            return arrayLength - 1;
        }
        return position;
    }


}
