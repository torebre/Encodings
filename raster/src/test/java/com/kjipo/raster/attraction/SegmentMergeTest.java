package com.kjipo.raster.attraction;

import com.google.common.collect.ImmutableList;
import com.kjipo.raster.EncodingUtilities;
import visualization.RasterRun;
import visualization.RasterVisualizer2;
import visualization.attraction.AttractionCellPainter;

import java.util.ArrayList;
import java.util.List;

public class SegmentMergeTest {


    public static void segmentMergeTest() throws InterruptedException {
        boolean mutableRaster[][] = new boolean[10][10];
        boolean prototype[][] = new boolean[10][10];

        for (int i = 0; i < 6; ++i) {
            prototype[8][i + 2] = true;
        }

        for (int i = 0; i < 3; ++i) {
            mutableRaster[3][i] = true;
        }

        boolean startRaster[][] = EncodingUtilities.copyRaster(mutableRaster);


        List<boolean[][]> rasters = new ArrayList<>();

        rasters.add(startRaster);

        do {
            SegmentMerge.mergeSegments(mutableRaster, prototype);
            boolean rasterCopy[][] = EncodingUtilities.copyRaster(mutableRaster);
            rasters.add(rasterCopy);
        }
        while (!rastersEqual(rasters.get(rasters.size() - 2), rasters.get(rasters.size() - 1)));


        RasterRun<AttractionCell> rasterRun = new RasterRun<AttractionCell>() {
            private int counter = 0;

            @Override
            public boolean[][] getRawInput() {
                return startRaster;
            }

            @Override
            public boolean hasNext() {
                return counter < rasters.size() - 1;
            }

            @Override
            public int getColumns() {
                return startRaster[0].length;
            }

            @Override
            public int getRows() {
                return startRaster.length;
            }

            @Override
            public AttractionCell getCell(int row, int column) {
                return new AttractionCell(row, column, rasters.get(counter), prototype);
            }

            @Override
            public void next() {
                ++counter;
            }
        };

        RasterVisualizer2.showRasterFlow(rasterRun,
                ImmutableList.of(new AttractionCellPainter()));


    }


    private static boolean rastersEqual(boolean raster1[][], boolean raster2[][]) {
        for (int row = 0; row < raster1.length; ++row) {
            for (int column = 0; column < raster2.length; ++column) {
                if (raster1[row][column] != raster2[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }


    public static void main(String args[]) throws InterruptedException {
        segmentMergeTest();

    }


}
