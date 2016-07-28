package raster;

import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.List;


/**
 * Data holder for a boolean encoding of a kanji.
 *
 */
public final class BooleanEncoding {
    final List<Source> sources = new ArrayList<>();
    final boolean raster[][];
    Complex flowRaster[][];


    public BooleanEncoding(boolean raster[][]) {
        this(raster, BooleanEncodingUtilities.setupFlowRaster(raster));
    }

    public BooleanEncoding(boolean raster[][], Complex flowRaster[][]) {
        this.raster = raster;
        this.flowRaster = flowRaster;
    }

    public BooleanEncoding(BooleanEncoding booleanEncoding) {
        booleanEncoding.sources.stream().map(source -> new Source(source))
                .forEach(copySource -> sources.add(copySource));
        int rowCounter = 0;
        raster = new boolean[booleanEncoding.raster.length][0];
        for(boolean row[] : booleanEncoding.raster) {
            boolean rasterRow[] = new boolean[row.length];
            System.arraycopy(row, 0, rasterRow, 0, row.length);
            raster[rowCounter++] = rasterRow;
        }
        flowRaster = new Complex[booleanEncoding.flowRaster.length][booleanEncoding.flowRaster[0].length];
        for(int row = 0; row < booleanEncoding.flowRaster.length; ++row) {
            for(int column = 0; column < booleanEncoding.flowRaster[0].length; ++column) {
                flowRaster[row][column] = new Complex(booleanEncoding.flowRaster[row][column].getReal(),
                        booleanEncoding.flowRaster[row][column].getImaginary());
            }
        }
    }

    public Complex[][] getFlowRaster() {
        return flowRaster;
    }


    public boolean[][] getRaster() {
        return raster;
    }

}
