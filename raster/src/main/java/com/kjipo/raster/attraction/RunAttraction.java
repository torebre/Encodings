package com.kjipo.raster.attraction;

import com.kjipo.prototype.Prototype;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.stochasticflow.StochasticFlowRasterImpl;

import java.util.List;

public class RunAttraction {



    public List<StochasticFlowRasterImpl> createRun(boolean raster[][]) {
        List<Segment> segments = divideIntoSegments(raster);

        Segment segment = segments.get(0);


        // TODO
        return null;



    }



    private List<Segment> divideIntoSegments(boolean raster[][]) {
        // TODO

        return null;



    }


    private void shiftCell(int row, int column, boolean raster[][], Prototype prototype) {
        for (FlowDirection flowDirection : FlowDirection.values()) {
            if(!EncodingUtilities.validCell(row, column, flowDirection, raster.length, raster[0].length)) {
                continue;
            }






        }








    }


}
