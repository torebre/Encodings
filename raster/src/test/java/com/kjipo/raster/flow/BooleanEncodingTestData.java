package com.kjipo.raster.flow;

import com.google.common.base.Preconditions;

import java.util.stream.IntStream;


public final class BooleanEncodingTestData {


    private BooleanEncodingTestData() {

    }


    public static boolean[][] getTestRaster1() {
        boolean raster[][] = new boolean[5][5];
        IntStream.range(0, raster.length)
                .forEach(row -> IntStream.range(0, raster[0].length)
                        .forEach(column -> raster[row][column] = row > column));
        return raster;
    }

    public static boolean[][] getTestRaster2() {
        boolean raster[][] = new boolean[2][2];
        IntStream.range(0, raster.length)
                .forEach(row -> IntStream.range(0, raster[0].length)
                        .forEach(column -> raster[row][column] = row == column));
        return raster;
    }

    public static boolean[][] getTestRaster3() {
        boolean raster[][] = new boolean[5][10];
        IntStream.range(0, raster.length)
                .forEach(row -> IntStream.range(0, raster[0].length)
                        .forEach(column -> raster[row][column] = row == 2));
        return raster;
    }

    public static boolean[][] getTestRaster4(int rows, int columns) {
        Preconditions.checkArgument(rows >= 5 && columns >= 20);
        boolean raster[][] = new boolean[rows][columns];
        for (int i = 5; i < 15; ++i) {
            raster[1][i] = true;
        }
        return raster;
    }


    /**
     * An open square
     */
    public static boolean[][] getTestRaster5() {
        boolean raster[][] = new boolean[10][10];

        for (int row = 0; row < 10; ++row) {
            for (int column = 0; column < 10; ++column) {
                if ((row == 3 || row == 7 || column == 3 || column == 7)
                        && !(row < 3 || row > 7 || column < 3 || column > 7)) {
                    raster[row][column] = true;
                }
            }
        }
        return raster;
    }


}
