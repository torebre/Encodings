package com.kjipo.raster.stochasticflow;


import com.google.common.collect.ImmutableMap;
import com.kjipo.representation.raster.FlowDirection;

import java.util.Map;

public final class StochasticFlowUtilities {

    public static final Map<FlowDirection, FlowDirection> OPPOSITE_DISTRIBUTION_MAP = ImmutableMap.<FlowDirection, FlowDirection>builder()
            .put(FlowDirection.EAST, FlowDirection.WEST)
            .put(FlowDirection.NORTH_EAST, FlowDirection.SOUTH_WEST)
            .put(FlowDirection.NORTH, FlowDirection.SOUTH)
            .put(FlowDirection.NORTH_WEST, FlowDirection.SOUTH_EAST)
            .put(FlowDirection.WEST, FlowDirection.EAST)
            .put(FlowDirection.SOUTH_WEST, FlowDirection.NORTH_EAST)
            .put(FlowDirection.SOUTH, FlowDirection.NORTH)
            .put(FlowDirection.SOUTH_EAST, FlowDirection.NORTH_WEST)
            .build();


    private StochasticFlowUtilities() {

    }


}
