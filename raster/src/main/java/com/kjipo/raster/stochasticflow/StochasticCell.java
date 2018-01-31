package com.kjipo.raster.stochasticflow;

import com.kjipo.raster.AbstractCell;
import com.kjipo.raster.FlowDirection;

public class StochasticCell extends AbstractCell {
    private final StochasticFlowRaster stochasticFlowRaster;


    protected StochasticCell(int row, int column, StochasticFlowRaster stochasticFlowRaster) {
        super(row, column);
        this.stochasticFlowRaster = stochasticFlowRaster;
    }


    public FlowDirection getFlowDirectionInCell() {
        return stochasticFlowRaster.getFlowDirectionInCell(getRow(), getColumn());
    }

    public int getFlowInCell() {
        return stochasticFlowRaster.getFlowInCell(getRow(), getColumn());
    }


}
