package com.kjipo.raster.attraction;

import visualization.AbstractCell;

public class AttractionCell extends AbstractCell {
    private final boolean raster[][];
    private final boolean protoypeRaster[][];


    protected AttractionCell(int row, int column, boolean raster[][], boolean prototypeRaster[][]) {
        super(row, column);
        this.raster = raster;
        this.protoypeRaster = prototypeRaster;
    }

    public boolean isFilled() {
        return raster[row][column];
    }

    public boolean containsPrototypeCell() {
        return protoypeRaster[row][column];
    }
}
