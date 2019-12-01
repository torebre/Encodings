package com.kjipo.raster.stochasticflow;


import com.kjipo.raster.AbstractCell;
import com.kjipo.representation.raster.FlowDirection;

import java.util.Collection;
import java.util.List;


public interface StochasticFlowRaster {

    int getRows();

    int getColumns();

    int getFlowIntoCell(int row, int column, FlowDirection flowDirection);

    int getFlowInCell(int row, int column);

    FlowDirection getFlowDirectionInCell(int row, int column);

    List<Source> getSources();

    Collection<? extends AbstractCell> getProcessedCells();

    Collection<? extends AbstractCell> getCellsToProcessNext();

}
