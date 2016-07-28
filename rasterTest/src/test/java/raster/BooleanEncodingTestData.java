package raster;

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

    public static boolean[][] getTestRaster4() {
        boolean raster[][] = new boolean[5][20];
        for(int i = 5; i < 15; ++i) {
            raster[1][i] = true;
        }
        return raster;
    }


}
