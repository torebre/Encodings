package com.kjipo.raster.flow;


import com.google.common.primitives.Ints;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;



public class BooleanEncodingUtilitiesTest {


    @Test
    public void getBorderTest() {
        boolean testRaster[][] = BooleanEncodingTestData.getTestRaster2();
        int border[][] = BooleanEncodingUtilities.getBorder(testRaster);

        assertThat(Ints.concat(border)).isEqualTo(new int[] {15, 0, 0, 15});
    }

    @Test
    public void getBorderTest4() {
        boolean testRaster[][] = BooleanEncodingTestData.getTestRaster4();
        int border[][] = BooleanEncodingUtilities.getBorder(testRaster);

        assertThat(border.length).isEqualTo(5);
        for(int borderLine[] : border) {
            assertThat(borderLine.length).isEqualTo(20);
        }

//        assertThat(Ints.concat(border)).isEqualTo(new int[] {15, 0, 0, 15});
    }




}
