package com.kjipo.raster.attraction;

import visualization.AbstractCell;

public class AttractionCell extends AbstractCell {
    private final boolean raster[][];


    protected AttractionCell(int row, int column, boolean raster[][]) {
        super(row, column);
        this.raster = raster;
    }

    public boolean isFilled() {
        return raster[row][column];
    }
}
