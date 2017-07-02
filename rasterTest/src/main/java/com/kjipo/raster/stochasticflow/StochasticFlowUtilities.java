package com.kjipo.raster.stochasticflow;


import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.fraction.Fraction;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.TileType;

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


    static void determineOutput(int x, int y, StochasticFlowRaster stochasticFlowRaster, boolean rawRaster[][]) {
        Fraction outputFlow[] = new Fraction[FlowDirection.values().length];
        for (Fraction fraction : outputFlow) {
            fraction = new Fraction(0);
        }

        for(FlowDirection flowDirection : FlowDirection.values()) {
            int flowIntoCell = stochasticFlowRaster.getFlowIntoCell(x, y, flowDirection);
            Fraction oppositeFlow = outputFlow[OPPOSITE_DISTRIBUTION_MAP.get(flowDirection).ordinal()];

            oppositeFlow = oppositeFlow.add(flowIntoCell);
        }

        TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(x, y, rawRaster);

        for(FlowDirection flowDirection : FlowDirection.values()) {
            switch(tileTypes[flowDirection.ordinal()]) {
                case OUTSIDE_SCREEN:
                    outputFlow[flowDirection.ordinal()] = new Fraction(0);
                    break;

                case OUTSIDE_CHARACTER:
                    // TODO
                    break;

                case OPEN:
                    // TODO
                    break;

                default:



            }

        }




    }





}
