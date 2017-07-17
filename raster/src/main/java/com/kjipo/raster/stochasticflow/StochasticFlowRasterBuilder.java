package com.kjipo.raster.stochasticflow;

import com.kjipo.raster.AbstractCell;
import com.kjipo.raster.FlowDirection;

import java.util.*;

public final class StochasticFlowRasterBuilder {
    private final int flows[][];
    private final FlowDirection flowDirections[][];
    private final List<Source> sources = new ArrayList<>();
    private final Set<AbstractCell> cellsToProcessNext = new HashSet<>();
    private final Set<AbstractCell> processedCells = new HashSet<>();


    private StochasticFlowRasterBuilder(int rows, int columns) {
        flows = new int[rows][columns];
        flowDirections = new FlowDirection[rows][columns];

        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++columns) {
                flows[row][column] = 0;
            }

        }

    }


    public static StochasticFlowRasterBuilder builder(int rows, int columns) {
        return new StochasticFlowRasterBuilder(rows, columns);
    }

    public StochasticFlowRasterBuilder setValue(int row, int column, int flow, FlowDirection flowDirection) {
        flows[row][column] = flow;
        flowDirections[row][column] = flowDirection;
        return this;
    }

    public StochasticFlowRasterBuilder addSource(int row, int column, FlowDirection flowDirection) {
        sources.add(new Source(row, column, flowDirection));
        return this;
    }

    public StochasticFlowRasterBuilder addProcessedCells(Collection<? extends AbstractCell> processedCells) {
        this.processedCells.addAll(processedCells);
        return this;
    }

    public StochasticFlowRasterBuilder addCellsToProcessNext(Collection<? extends AbstractCell> cellsToProcessNext) {
        this.cellsToProcessNext.addAll(cellsToProcessNext);
        return this;
    }

    public StochasticFlowRasterBuilder addSource(Source source) {
        sources.add(source);
        return this;
    }

    public StochasticFlowRasterImpl build() {
        return new StochasticFlowRasterImpl(flows, flowDirections, sources, processedCells, cellsToProcessNext);
    }


}
