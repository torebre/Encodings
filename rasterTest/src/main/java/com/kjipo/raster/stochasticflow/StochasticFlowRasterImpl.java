package com.kjipo.raster.stochasticflow;

import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;

public class StochasticFlowRasterImpl implements StochasticFlowRaster {
    private final int flows[][];
    private final FlowDirection flowDirections[][];


    public StochasticFlowRasterImpl(int flows[][], FlowDirection flowDirections[][]) {
        this.flows = flows;
        this.flowDirections = flowDirections;
    }

    @Override
    public int getRows() {
        return flows.length;
    }

    @Override
    public int getColumns() {
        return flows[0].length;
    }

    @Override
    public int getFlowIntoCell(int x, int y, FlowDirection flowDirection) {
        if (!EncodingUtilities.validCell(x, y, flowDirection, flows.length, flows[0].length)) {
            return 0;
        }
        int rowShift = x + flowDirection.getRowShift();
        int columnShift = y + flowDirection.getColumnShift();

        if (flowDirections[rowShift][columnShift] == flowDirection) {
            return flows[rowShift][columnShift];
        }

        return 0;
    }

    @Override
    public int getFlowInCell(int row, int column) {
        return flows[row][column];
    }

    @Override
    public FlowDirection getFlowDirectionInCell(int row, int column) {
        return flowDirections[row][column];
    }
}
