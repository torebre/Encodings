package com.kjipo.raster.stochasticflow;

import com.kjipo.raster.AbstractCell;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class StochasticFlowRasterImpl implements StochasticFlowRaster {
    private final int flows[][];
    private final FlowDirection flowDirections[][];
    private final List<Source> sources;
    private final Collection<? extends AbstractCell> processedCells;
    private final Collection<? extends AbstractCell> cellsToProcessNext;


    private static final Logger LOG = LoggerFactory.getLogger(StochasticFlowRasterImpl.class);


    public StochasticFlowRasterImpl(int[][] flows, FlowDirection[][] flowDirections,
                                    List<Source> sources, Collection<? extends AbstractCell> processedCells,
                                    Collection<? extends AbstractCell> cellsToProcessNext) {
        this.flows = flows;
        this.flowDirections = flowDirections;
        this.sources = sources;
        this.processedCells = processedCells;
        this.cellsToProcessNext = cellsToProcessNext;
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

    @Override
    public List<Source> getSources() {
        return sources;
    }

    @Override
    public Collection<? extends AbstractCell> getProcessedCells() {
        return processedCells;
    }

    @Override
    public Collection<? extends AbstractCell> getCellsToProcessNext() {
        return cellsToProcessNext;
    }
}
