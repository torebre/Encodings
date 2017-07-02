package com.kjipo.raster.stochasticflow;


import com.kjipo.raster.FlowDirection;


public interface StochasticFlowRaster {

    int getRows();

    int getColumns();

    int getFlowIntoCell(int row, int column, FlowDirection flowDirection);

    int getFlowInCell(int row, int column);

    FlowDirection getFlowDirectionInCell(int row, int column);


}
