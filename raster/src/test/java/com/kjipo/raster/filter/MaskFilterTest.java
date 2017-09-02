package com.kjipo.raster.filter;

import org.junit.Test;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MaskFilterTest {


    @Test
    public void filterTest() {
        MaskFilter maskFilter = new MaskFilter();
        boolean input[][] = new boolean[3][3];

        input[0][0] = true;
        input[0][1] = true;
        input[0][2] = false;
        input[1][0] = true;
        input[1][1] = true;
        input[1][2] = false;
        input[2][0] = true;
        input[2][1] = true;
        input[2][2] = false;

        boolean expectedResult[][] = new boolean[3][3];

        expectedResult[0][0] = true;
        expectedResult[0][1] = false;
        expectedResult[0][2] = false;
        expectedResult[1][0] = true;
        expectedResult[1][1] = false;
        expectedResult[1][2] = false;
        expectedResult[2][0] = true;
        expectedResult[2][1] = false;
        expectedResult[2][2] = false;

        System.out.println("Input:");
        printRaster(input, System.out);

        System.out.println("Expected result:");
        printRaster(expectedResult, System.out);

        List<boolean[][]> results = maskFilter.applyFilter(input);
        boolean result[][] = results.get(results.size() - 1);

        System.out.println("Result:");
        printRaster(result, System.out);

        assertThat(result).hasSize(3);
        assertThat(expectedResult).hasSize(3);

        for(int i = 0; i < result.length; ++i) {
            assertThat(result[i]).hasSize(3);
            assertThat(expectedResult[i]).hasSize(3);

            for(int j = 0; j < result[0].length; ++j) {
                assertThat(result[i][j]).isEqualTo(expectedResult[i][j]);
            }
        }
    }

//    @Test
//    public void filterTest2() {
//        MaskFilter maskFilter = new MaskFilter();
//        boolean input[][] = new boolean[3][3];
//
//        input[0][0] = true;
//        input[0][1] = true;
//        input[0][2] = false;
//        input[1][0] = true;
//        input[1][1] = true;
//        input[1][2] = false;
//        input[2][0] = true;
//        input[2][1] = true;
//        input[2][2] = false;
//
//        boolean expectedResult[][] = new boolean[3][3];
//
//        expectedResult[0][0] = true;
//        expectedResult[0][1] = false;
//        expectedResult[0][2] = false;
//        expectedResult[1][0] = true;
//        expectedResult[1][1] = false;
//        expectedResult[1][2] = false;
//        expectedResult[2][0] = true;
//        expectedResult[2][1] = false;
//        expectedResult[2][2] = false;
//
//        System.out.println("Input:");
//        printRaster(input, System.out);
//
//        System.out.println("Expected result:");
//        printRaster(expectedResult, System.out);
//
//        List<boolean[][]> results = maskFilter.applyFilter(input);
//        boolean result[][] = results.get(results.size() - 1);
//
//        System.out.println("Result:");
//        printRaster(result, System.out);
//
//        assertThat(result).hasSize(3);
//        assertThat(expectedResult).hasSize(3);
//
//        for(int i = 0; i < result.length; ++i) {
//            assertThat(result[i]).hasSize(3);
//            assertThat(expectedResult[i]).hasSize(3);
//
//            for(int j = 0; j < result[0].length; ++j) {
//                assertThat(result[i][j]).isEqualTo(expectedResult[i][j]);
//            }
//        }
//
//    }



    private static void printRaster(boolean raster[][], PrintStream printStream) {
        IntStream.range(0, raster.length).forEach(row -> {
            IntStream.range(0, raster[0].length).forEach(column ->
                    printStream.print(raster[row][column] ? "X" : "0"));
            printStream.print("\n");
        });
    }


}
