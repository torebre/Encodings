package com.kjipo.raster.stochasticflow;


import com.kjipo.raster.FlowDirection;

public interface StochasticFlowRaster {

    boolean[][] getRawRaster();

    int getFlowIntoCell(int x, int y, FlowDirection flowDirection);


}
