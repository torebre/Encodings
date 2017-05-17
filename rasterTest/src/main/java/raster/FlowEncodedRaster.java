package raster;

import org.apache.commons.math3.complex.Complex;

public interface FlowEncodedRaster {

    Complex[][] getFlowRaster();

    boolean[][] getRaster();

}
