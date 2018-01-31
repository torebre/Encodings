package com.kjipo.raster;

public class Cell extends AbstractCell {
    private final FlowDirection flowDirection;
    private final int flowStrength;

    public Cell(int row, int column, FlowDirection flowDirection, int flowStrength) {
        super(row, column);
        this.flowDirection = flowDirection;
        this.flowStrength = flowStrength;
    }

    public FlowDirection getFlowDirection() {
        return flowDirection;
    }

    public int getFlowStrength() {
        return flowStrength;
    }
}
