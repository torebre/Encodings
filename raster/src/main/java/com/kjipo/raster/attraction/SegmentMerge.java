package com.kjipo.raster.attraction;

import com.kjipo.raster.EncodingUtilities;
import com.kjipo.representation.raster.FlowDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SegmentMerge {

    private static final Logger LOG = LoggerFactory.getLogger(SegmentMerge.class);


    public static void mergeSegments(boolean mutableRaster[][], boolean prototype[][]) {
        boolean stop = false;

        for (int row = 0; row < mutableRaster.length; ++row) {
            for (int column = 0; column < mutableRaster[0].length; ++column) {
                if (stop) {
                    return;
                }
                if (mutableRaster[row][column]) {

                    int north = 0;
                    int south = 0;
                    int east = 0;
                    int west = 0;

                    for (int i = row; i < prototype.length; ++i) {
                        for (int j = column; j < prototype[0].length; ++j) {
                            if (prototype[i][j]) {
                                if (i < row) {
                                    north++;
                                } else if (i > row) {
                                    south++;
                                }

                                if (j < column) {
                                    west++;
                                } else if (j > column) {
                                    east++;
                                }
                            }
                        }
                    }

                    boolean hasMoved = false;

                    if (north > south && west > east) {
                        hasMoved = attemptCellShift(FlowDirection.NORTH_WEST, row, column, mutableRaster);
                    }

                    if (hasMoved) {
                        stop = true;
                        continue;
                    }

                    if (north > south && west < east) {
                        hasMoved = attemptCellShift(FlowDirection.NORTH_EAST, row, column, mutableRaster);
                    }

                    if (hasMoved) {
                        stop = true;
                        continue;
                    }

                    if (south < north && west > east) {
                        hasMoved = attemptCellShift(FlowDirection.SOUTH_WEST, row, column, mutableRaster);
                    }

                    if (hasMoved) {
                        stop = true;
                        continue;
                    }

                    if (south < north && west < east) {
                        hasMoved = attemptCellShift(FlowDirection.SOUTH_EAST, row, column, mutableRaster);
                    }

                    if (hasMoved) {
                        stop = true;
                        continue;
                    }


                    if (north > south) {
                        hasMoved = attemptCellShift(FlowDirection.NORTH, row, column, mutableRaster);
                    }

                    if (hasMoved) {
                        stop = true;
                        continue;
                    }


                    if (north < south) {
                        hasMoved = attemptCellShift(FlowDirection.SOUTH, row, column, mutableRaster);
                    }

                    if (hasMoved) {
                        stop = true;
                        continue;
                    }


                    if (west > east) {
                        hasMoved = attemptCellShift(FlowDirection.WEST, row, column, mutableRaster);
                    }

                    if (hasMoved) {
                        stop = true;
                        continue;
                    }


                    if (west < east) {
                        hasMoved = attemptCellShift(FlowDirection.EAST, row, column, mutableRaster);
                    }


                    if (hasMoved) {
                        stop = true;
                        continue;

                    }

                    // TODO


                }


            }


        }


    }

    private static boolean attemptCellShift(FlowDirection flowDirection, int row, int column, boolean mutableRaster[][]) {
        if (EncodingUtilities.validCell(row, column, flowDirection, mutableRaster.length, mutableRaster[0].length)
                && !isCellDetached(flowDirection, row, column, mutableRaster)
                && !mutableRaster[row + flowDirection.getRowShift()][column + flowDirection.getColumnShift()]) {
            mutableRaster[row][column] = false;
            mutableRaster[row + flowDirection.getRowShift()][column + flowDirection.getColumnShift()] = true;

            LOG.info("Shifting cell {},{}: {}", row, column, flowDirection);

            return true;
        }
        return false;
    }

    private static boolean isCellDetached(FlowDirection flowDirection, int row, int column, boolean raster[][]) {
        return isCellDetached(row + flowDirection.getRowShift(), column + flowDirection.getColumnShift(), row, column, raster);
    }

    private static boolean isCellDetached(int row, int column, int originalRow, int originalColumn, boolean raster[][]) {
        boolean detached = true;

        for (FlowDirection flowDirection : FlowDirection.values()) {
            int shiftedRow = row + flowDirection.getRowShift();
            int shiftedColumn = column + flowDirection.getColumnShift();

            if ((shiftedRow == originalRow && shiftedColumn == originalColumn)
                    || !EncodingUtilities.validCoordinates(shiftedRow, shiftedColumn, raster.length, raster[0].length)) {
                continue;
            }

            if (raster[shiftedRow][shiftedColumn]) {
                detached = false;
                break;
            }

        }

        return detached;

    }


}
