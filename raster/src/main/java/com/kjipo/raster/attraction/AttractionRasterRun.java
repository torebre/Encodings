package com.kjipo.raster.attraction;

import com.kjipo.visualization.RasterRun;

public class AttractionRasterRun implements RasterRun<AttractionCell> {


    @Override
    public boolean[][] getRawInput() {
        return new boolean[0][];
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public int getColumns() {
        return 0;
    }

    @Override
    public int getRows() {
        return 0;
    }

    @Override
    public AttractionCell getCell(int row, int column) {
        return null;
    }

    @Override
    public void next() {

    }
}
