package com.kjipo.raster.stochasticflow;


import com.kjipo.representation.raster.FlowDirection;

public class Source {
    private final int row;
    private final int column;
    private final FlowDirection flowDirection;


    public Source(int row, int column, FlowDirection flowDirection) {
        this.row = row;
        this.column = column;
        this.flowDirection = flowDirection;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public FlowDirection getFlowDirection() {
        return flowDirection;
    }
}
