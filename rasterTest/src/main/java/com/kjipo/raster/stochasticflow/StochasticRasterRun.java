package com.kjipo.raster.stochasticflow;

import visualization.RasterRun;

import java.util.List;

public class StochasticRasterRun implements RasterRun<StochasticCell> {
    private final List<StochasticFlowRasterImpl> rasters;
    private final boolean rawRaster[][];
    private final int rows;
    private final int columns;

    private int current = 0;


    public StochasticRasterRun(List<StochasticFlowRasterImpl> rasters, int rows, int columns, boolean rawRaster[][]) {
        this.rasters = rasters;
        this.rows = rows;
        this.columns = columns;
        this.rawRaster = rawRaster;
    }

    @Override
    public boolean[][] getRawInput() {
        return rawRaster;
    }

    @Override
    public boolean hasNext() {
        return current < rasters.size() - 1;
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public StochasticCell getCell(int row, int column) {
        StochasticFlowRasterImpl stochasticFlowRaster = rasters.get(current);

        // TODO

        return new StochasticCell(row, column, stochasticFlowRaster);
    }

    @Override
    public void next() {
        ++current;
    }
}
