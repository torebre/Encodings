package com.kjipo.raster.filter;

import com.google.common.collect.ImmutableMap;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.representation.raster.FlowDirection;
import com.kjipo.representation.raster.TileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MaskFilter implements Filter {

    private final static Map<Key, boolean[]> signatureMap;


    static {
        signatureMap = ImmutableMap.<Key, boolean[]>builder().put(getKey(signatureToArray(new boolean[][]{
                        {true, true, false},
                        {true, true, false},
                        {true, true, false},
                })),
                signatureToArray(new boolean[][]{
                        {true, false, false},
                        {true, false, false},
                        {true, false, false},
                }))
                .put(getKey(signatureToArray(new boolean[][]{
                                {true, true, true},
                                {true, true, true},
                                {false, false, false},
                        })),
                        signatureToArray(new boolean[][]{
                                {true, true, true},
                                {false, false, false},
                                {false, false, false},
                        }))
                .put(getKey(signatureToArray(new boolean[][]{
                                {false, true, true},
                                {false, true, true},
                                {false, true, true},
                        })),
                        signatureToArray(new boolean[][]{
                                {false, false, true},
                                {false, false, true},
                                {false, false, true},
                        }))
                .put(getKey(signatureToArray(new boolean[][]{
                                {false, false, false},
                                {true, true, true},
                                {true, true, true},
                        })),
                        signatureToArray(new boolean[][]{
                                {false, false, false},
                                {false, false, false},
                                {true, true, true},
                        }))
                .build();

    }


    private static final Logger LOG = LoggerFactory.getLogger(MaskFilter.class);


    @Override
    public List<boolean[][]> applyFilter(boolean[][] raster) {
//        for (Map.Entry<Key, boolean[]> entry : signatureMap.entrySet()) {
//
//            System.out.print("Key: ");
//            for (boolean b : entry.getKey().get()) {
//                System.out.print(b +",");
//            }
//            System.out.println();
//
//            System.out.print("Value: ");
//            for (boolean b : entry.getValue()) {
//                System.out.print(b +",");
//            }
//            System.out.println();
//
//
//        }


        boolean repeat;
        List<boolean[][]> results = new ArrayList<>();

        do {

            LOG.info("Running filter");

            boolean result[][] = results.isEmpty() ? EncodingUtilities.copyRaster(raster) : EncodingUtilities.copyRaster(results.get(results.size() - 1));
            repeat = false;
            for (int row = 1; row < raster.length - 1; ++row) {
                for (int column = 1; column < raster[0].length - 1; ++column) {
                    if (!result[row][column]) {
                        continue;
                    }

                    boolean[] neighbours = tilesToArray(result[row][column], EncodingUtilities.determineNeighbourTypes(row, column, result));

                    Key equivalenceWrapper = getKey(neighbours);
                    if (signatureMap.containsKey(equivalenceWrapper)) {

                        if (LOG.isInfoEnabled()) {
                            LOG.info("Found hit at {}, {}", row, column);
                        }

                        applySignature(row, column, signatureMap.get(equivalenceWrapper), result);
                        repeat = true;
                    }
                }
            }
            results.add(result);
        }
        while (repeat);

        return results;
    }


    private static boolean[] signatureToArray(boolean signature[][]) {
        return new boolean[]{
                signature[1][1],
                signature[1][2],
                signature[0][2],
                signature[0][1],
                signature[0][0],
                signature[1][0],
                signature[2][0],
                signature[2][1],
                signature[2][2]
        };
    }

    private static boolean[] tilesToArray(boolean centerCell, TileType tileArray[]) {
        boolean result[] = new boolean[9];
        result[0] = centerCell;
        int counter = 1;

        for (TileType tileType : tileArray) {
            if (tileType == TileType.OPEN) {
                result[counter] = true;
            }
            ++counter;
        }

        return result;
    }

    private static class Key {
        private final boolean data[];


        private Key(boolean[] data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return Arrays.equals(data, key.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }
    }


    private static Key getKey(boolean signature[]) {
        return new Key(signature);
    }

    private static void applySignature(int row, int column, boolean signature[], boolean raster[][]) {
        raster[row][column] = signature[0];
        int counter = 1;
        for (FlowDirection flowDirection : FlowDirection.values()) {
            raster[row + flowDirection.getRowShift()][column + flowDirection.getColumnShift()] = signature[counter++];
        }
    }


    private static void printRaster(boolean raster[][], PrintStream printStream) {
        IntStream.range(0, raster.length).forEach(row -> {
            IntStream.range(0, raster[0].length).forEach(column ->
                    printStream.print(raster[row][column] ? "X" : "0"));
            printStream.print("\n");
        });
    }


}
