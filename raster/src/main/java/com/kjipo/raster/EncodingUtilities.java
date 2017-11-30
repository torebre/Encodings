package com.kjipo.raster;


import com.kjipo.raster.segment.Pair;

import java.util.Collection;
import java.util.Optional;

public final class EncodingUtilities {


    private EncodingUtilities() {

    }

    public static boolean[][] copyRaster(boolean raster[][]) {
        boolean result[][] = new boolean[raster.length][raster[0].length];

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                result[row][column] = raster[row][column];
            }
        }
        return result;
    }


    public static TileType[] determineNeighbourTypes(int row, int column, boolean raster[][]) {
        TileType result[] = new TileType[8];

        if (column == raster[0].length - 1) {
            result[0] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row][column + 1]) {
            result[0] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[0] = TileType.OPEN;
        }

        if (row == 0 || column == raster[0].length - 1) {
            result[1] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row - 1][column + 1]) {
            result[1] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[1] = TileType.OPEN;
        }

        if (row == 0) {
            result[2] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row - 1][column]) {
            result[2] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[2] = TileType.OPEN;
        }

        if (row == 0 || column == 0) {
            result[3] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row - 1][column - 1]) {
            result[3] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[3] = TileType.OPEN;
        }

        if (column == 0) {
            result[4] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row][column - 1]) {
            result[4] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[4] = TileType.OPEN;
        }

        if (row == raster.length - 1 || column == 0) {
            result[5] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row + 1][column - 1]) {
            result[5] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[5] = TileType.OPEN;
        }

        if (row == raster.length - 1) {
            result[6] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row + 1][column]) {
            result[6] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[6] = TileType.OPEN;
        }

        if (row == raster.length - 1 || column == raster[0].length - 1) {
            result[7] = TileType.OUTSIDE_SCREEN;
        } else if (!raster[row + 1][column + 1]) {
            result[7] = TileType.OUTSIDE_CHARACTER;
        } else {
            result[7] = TileType.OPEN;
        }

        return result;
    }


    public static boolean validCell(int row, int column, FlowDirection flowDirection, int rows, int columns) {
        return validCoordinates(row + flowDirection.getRowShift(),
                column + flowDirection.getColumnShift(), rows, columns);
    }

    public static boolean validCoordinates(int shiftedRow, int shiftedColumn, int rows, int columns) {
        if (shiftedRow < 0 || shiftedRow >= rows) {
            return false;
        }

        if (shiftedColumn < 0 || shiftedColumn >= columns) {
            return false;
        }

        return true;
    }

    public static boolean validCellOppositeDirection(int row, int column, FlowDirection flowDirection, int rows, int columns) {
        int shiftedRow = row - flowDirection.getRowShift();
        if (shiftedRow < 0 || shiftedRow >= rows) {
            return false;
        }

        int shiftedColumn = column - flowDirection.getColumnShift();
        if (shiftedColumn < 0 || shiftedColumn >= columns) {
            return false;
        }

        return true;
    }


    public static boolean[][] computeRasterBasedOnPairs(int rows, int columns, Collection<Pair> pairs) {
        boolean result[][] = new boolean[rows][columns];
        pairs.forEach(pair -> result[pair.getRow()][pair.getColumn()] = true);
        return result;
    }

    public static FlowDirection determineOffset(int rowOffset, int columnOffset) {
        if (rowOffset == -1 && columnOffset == -1) {
            return FlowDirection.NORTH_WEST;
        }
        if (rowOffset == -1 && columnOffset == 0) {
            return FlowDirection.NORTH;
        }
        if (rowOffset == -1 && columnOffset == 1) {
            return FlowDirection.NORTH_EAST;
        }

        if (rowOffset == 0 && columnOffset == -1) {
            return FlowDirection.WEST;
        }
        if (rowOffset == 0 && columnOffset == 0) {
            return null;
        }
        if (rowOffset == 0 && columnOffset == 1) {
            return FlowDirection.EAST;
        }

        if (rowOffset == 1 && columnOffset == -1) {
            return FlowDirection.SOUTH_WEST;
        }
        if (rowOffset == 1 && columnOffset == 0) {
            return FlowDirection.SOUTH;
        }
        if (rowOffset == 1 && columnOffset == 1) {
            return FlowDirection.SOUTH_EAST;
        }

        return null;
    }

    public static FlowDirection computeDirection(Pair first, Pair second) {
        int rowChange = second.getRow() - first.getRow();
        int columnChange = second.getColumn() - first.getColumn();

        if (rowChange == 0 && columnChange == 1) {
            return FlowDirection.EAST;
        }
        if (rowChange == -1 && columnChange == 1) {
            return FlowDirection.NORTH_EAST;
        }
        if (rowChange == 1 && columnChange == 1) {
            return FlowDirection.SOUTH_EAST;
        }

        if (rowChange == -1 && columnChange == 0) {
            return FlowDirection.WEST;
        }
        if (rowChange == 1 && columnChange == 0) {
            return FlowDirection.EAST;
        }

        if (rowChange == 0 && columnChange == -1) {
            return FlowDirection.NORTH;
        }
        if (rowChange == -1 && columnChange == -1) {
            return FlowDirection.NORTH_WEST;
        }
        if (rowChange == 1 && columnChange == -1) {
            return FlowDirection.SOUTH_WEST;
        }

        return null;

    }
}
