package com.kjipo.raster;


public final class EncodingUtilities {


    private EncodingUtilities() {

    }


    public static TileType[] determineNeighbourTypes(int row, int column, boolean raster[][]) {
        TileType result[] = new TileType[8];

        if(column == raster[0].length - 1) {
            result[0] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row][column + 1]) {
            result[0] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[0] = TileType.OPEN;
        }

        if(row == 0 || column == raster[0].length - 1) {
            result[1] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row - 1][column + 1]) {
            result[1] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[1] = TileType.OPEN;
        }

        if(row == 0) {
            result[2] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row - 1][column]) {
            result[2] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[2] = TileType.OPEN;
        }

        if(row == 0 || column == 0) {
            result[3] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row - 1][column - 1]) {
            result[3] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[3] = TileType.OPEN;
        }

        if(column == 0) {
            result[4] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row][column - 1]) {
            result[4] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[4] = TileType.OPEN;
        }

        if(row == raster.length - 1 || column == 0) {
            result[5] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row + 1][column - 1]) {
            result[5] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[5] = TileType.OPEN;
        }

        if(row == raster.length - 1) {
            result[6] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row + 1][column]) {
            result[6] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[6] = TileType.OPEN;
        }

        if(row == raster.length - 1 || column == raster[0].length - 1) {
            result[7] = TileType.OUTSIDE_SCREEN;
        }
        else if(!raster[row + 1][column + 1]) {
            result[7] = TileType.OUTSIDE_CHARACTER;
        }
        else {
            result[7] = TileType.OPEN;
        }

        return result;
    }


    public static boolean validCell(int row, int column, FlowDirection flowDirection, int rows, int columns) {
        int shiftedRow = row + flowDirection.getRowShift();
        if(shiftedRow < 0 || shiftedRow >= rows) {
            return false;
        }

        int shiftedColumn = column + flowDirection.getColumnShift();
        if(shiftedColumn < 0 || shiftedColumn >= columns) {
            return false;
        }

        return true;
    }


}
