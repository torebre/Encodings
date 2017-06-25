package com.kjipo.raster.flow;


import org.apache.commons.math3.complex.Complex;
import com.kjipo.raster.flow.BooleanEncoding;
import com.kjipo.raster.flow.Point;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.stream.IntStream;


public final class BooleanEncodingUtilities {


    private BooleanEncodingUtilities() {

    }


    public static boolean isCharacterAtPoint(int row, int column, BooleanEncoding booleanEncoding) {
        return booleanEncoding.raster[row][column];
    }


    public static boolean isSourceAtPoint(int pRow, int pColumn, BooleanEncoding booleanEncoding) {
        return !booleanEncoding.sources.stream()
                .noneMatch(source -> source.getRow() == pRow && source.getColumn() == pColumn);
    }

    public static double getMaxLength(BooleanEncoding booleanEncoding) {
        return Arrays.stream(booleanEncoding.flowRaster)
                .flatMap(c -> Arrays.stream(c))
                .reduce(Complex.ZERO, (c2, m) -> c2.abs() > m.abs() ? c2 : m).abs();
    }

    public static Complex[][] setupFlowRaster(boolean pRaster[][]) {
        Complex matrix[][] = new Complex[pRaster.length][pRaster[0].length];
        IntStream.range(0, matrix.length)
                .forEach(i -> IntStream.range(0, matrix[0].length)
                        .forEach(j -> matrix[i][j] = new Complex(0, 0)));
        return matrix;
    }

    public static double getMaxLength(Collection<BooleanEncoding> encodings) {
        double max = Double.MIN_VALUE;
        for (BooleanEncoding booleanEncoding : encodings) {
            double maxForEncoding = getMaxLength(booleanEncoding);
            if (max < maxForEncoding) {
                max = maxForEncoding;
            }
        }
        return max;
    }

    public static int[][] getBorder(boolean raster[][]) {
        assertIsRectangle(raster);
        int result[][] = new int[raster.length][raster[0].length];

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (raster[row][column]) {
                    // Right
                    if (column == raster[0].length - 1 || !raster[row][column + 1]) {
                        result[row][column] = result[row][column] | 1;
                    }
                    // Top
                    if (row == 0 || !raster[row - 1][column]) {
                        result[row][column] = result[row][column] | 2;
                    }
                    // Left
                    if (column == 0 || !raster[row][column - 1]) {
                        result[row][column] = result[row][column] | 4;
                    }
                    if (row == raster.length - 1 || !raster[row + 1][column]) {
                        result[row][column] = result[row][column] | 8;
                    }
                }
            }
        }
        return result;
    }


    private static void assertIsQuadratic(boolean raster[][]) throws IllegalArgumentException {
        int lengthFirstRow = raster[0].length;
        if (lengthFirstRow != raster.length) {
            throw new IllegalArgumentException("Raster is not quadratic");
        }
        for (int row = 1; row < raster.length; ++row) {
            if (lengthFirstRow != raster[row].length) {
                throw new IllegalArgumentException("Raster is not quadratic");
            }
        }
    }

    private static void assertIsRectangle(boolean raster[][]) throws IllegalArgumentException {
        int lengthFirstRow = raster[0].length;
        for (int row = 1; row < raster.length; ++row) {
            if (lengthFirstRow != raster[row].length) {
                throw new IllegalArgumentException("Raster is not a rectangle");
            }
        }
    }


    public static Queue<Point> getPointsWithFlow(BooleanEncoding booleanEncoding) {
        Queue<Point> pointsWithFlow = new ArrayDeque<>();
        IntStream.range(0, booleanEncoding.flowRaster.length)
                .forEach(i -> IntStream.range(0, booleanEncoding.flowRaster[0].length)
                        .filter(j -> booleanEncoding.flowRaster[i][j].abs() > 0)
                        .forEach(j2 -> pointsWithFlow.add(new Point(i, j2))));
        return pointsWithFlow;
    }
}
