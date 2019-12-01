package com.kjipo.segmentation;

import com.kjipo.raster.Cell;
import com.kjipo.raster.filter.Filter;
import com.kjipo.raster.filter.MaskFilter;
import com.kjipo.raster.segment.Pair;
import com.kjipo.representation.raster.FlowDirection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class RasterTransformer {


    public static Cell[][] segmentTransformer(boolean raster[][]) {
        Filter maskFilter = new MaskFilter();
        List<boolean[][]> results = maskFilter.applyFilter(raster);
        boolean filteredImage[][] = results.get(results.size() - 1);

        Cell flowRaster[][] = createEmptyFlowRaster(filteredImage);
        Pair startPoint;

        while (true) {
            startPoint = getStartPoint(filteredImage, flowRaster);

            if (startPoint == null) {
                break;
            }

            boolean placedFlow = false;
            for (FlowDirection flowDirection : FlowDirection.values()) {
                int rowNeighbour = startPoint.getRow() + flowDirection.getRowShift();
                int columnNeighbour = startPoint.getColumn() + flowDirection.getColumnShift();

                if (rowNeighbour < 0 || rowNeighbour >= raster.length || columnNeighbour < 0 || columnNeighbour > raster[0].length) {
                    continue;
                }

                if (raster[rowNeighbour][columnNeighbour]) {
                    flowRaster[startPoint.getRow()][startPoint.getColumn()] = new Cell(startPoint.getRow(), startPoint.getColumn(),
                            flowDirection, 1);
                    placedFlow = true;
                    break;
                }
            }

            if (!placedFlow) {
                // A point that is by itself, set the flow to be north
                // just to let it be something
                flowRaster[startPoint.getRow()][startPoint.getColumn()] = new Cell(startPoint.getRow(), startPoint.getColumn(),
                        FlowDirection.NORTH, 1);
            }
            updateFlow(startPoint, filteredImage, flowRaster);
        }

        return flowRaster;
    }

    private static void updateFlow(Pair startPoint, boolean raster[][], Cell flowRaster[][]) {
        Queue<Pair> pointsToProcess = new ArrayDeque<>();
        pointsToProcess.add(startPoint);

        Pair point;
        while ((point = pointsToProcess.poll()) != null) {
            pointsToProcess.addAll(updateNeighbours(point.getRow(), point.getColumn(), raster, flowRaster));
        }
    }


    private static List<Pair> updateNeighbours(int row, int column, boolean raster[][], Cell flowRaster[][]) {
        List<Pair> updateCells = new ArrayList<>();

        for (FlowDirection flowDirection : FlowDirection.values()) {
            int rowNeighbour = row + flowDirection.getRowShift();
            int columnNeighbour = column + flowDirection.getColumnShift();

            if (rowNeighbour < 0
                    || rowNeighbour >= flowRaster.length
                    || columnNeighbour < 0
                    || columnNeighbour >= flowRaster[0].length) {
                continue;
            }

            if (flowRaster[rowNeighbour][columnNeighbour].getFlowStrength() == 0
                    && raster[rowNeighbour][columnNeighbour]) {
                flowRaster[rowNeighbour][columnNeighbour] = new Cell(rowNeighbour, columnNeighbour, flowDirection, flowDirection == flowRaster[row][column].getFlowDirection() ? flowRaster[row][column].getFlowStrength() + 1 : 1);
                updateCells.add(new Pair(rowNeighbour, columnNeighbour));
            }
        }
        return updateCells;
    }


    private static Cell[][] createEmptyFlowRaster(boolean raster[][]) {
        Cell result[][] = new Cell[raster.length][raster[0].length];

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                result[row][column] = new Cell(row, column, null, 0);
            }
        }
        return result;
    }


    private static Pair getStartPoint(boolean raster[][], Cell flowRaster[][]) {
        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (raster[row][column] && flowRaster[row][column].getFlowStrength() == 0) {
                    return new Pair(row, column);
                }
            }
        }
        return null;
    }


}
