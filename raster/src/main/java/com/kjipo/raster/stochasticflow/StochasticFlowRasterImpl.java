package com.kjipo.raster.stochasticflow;

import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visualization.RasterVisualizer2;

public class StochasticFlowRasterImpl implements StochasticFlowRaster {
    private final int flows[][];
    private final FlowDirection flowDirections[][];

    private static final Logger LOG = LoggerFactory.getLogger(StochasticFlowRasterImpl.class);


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
        if (!EncodingUtilities.validCellOppositeDirection(x, y, flowDirection, flows.length, flows[0].length)) {
            return 0;
        }
        int rowShift = x - flowDirection.getRowShift();
        int columnShift = y - flowDirection.getColumnShift();

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
