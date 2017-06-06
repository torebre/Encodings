package com.kjipo.setupUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.util.stream.IntStream;


public class RasterUtilities {
    private static final Logger LOG = LoggerFactory.getLogger(RasterUtilities.class);


    public static boolean[][] setupRaster(GlyphVector glyphVector, int height, int width) throws IllegalArgumentException {
        assertOnlyOneGlyphInGlyphVector(glyphVector);
        return paintOnRaster(glyphVector, height, width);
    }

    public static boolean[][] paintOnRaster(GlyphVector glyphVector, int pRows, int pColumns) {
        assertOnlyOneGlyphInGlyphVector(glyphVector);
        for(int i = 0; i < glyphVector.getNumGlyphs(); ++i) {
            Shape shape = glyphVector.getGlyphOutline(i);
            LOG.debug("Bounds {}, {}, {}, {}",
                    shape.getBounds().getMinX(), shape.getBounds().getMaxX(),
                    shape.getBounds().getMinY(), shape.getBounds().getMaxY());
        }
        return paintOnRaster(glyphVector.getGlyphOutline(0), pRows, pColumns);
    }

    private static void assertOnlyOneGlyphInGlyphVector(GlyphVector glyphVector) throws IllegalArgumentException {
        if(glyphVector.getNumGlyphs() > 1) {
            throw new IllegalArgumentException("Only one glyph supported for now");
        }
    }

    public static boolean[][] paintOnRaster(Shape shape, int rows, int columns) {
        Rectangle bounds = shape.getBounds();
        AffineTransform transform = new AffineTransform();
        transform.translate(-bounds.getMinX(), -bounds.getMinY());
        Shape transformedShape = transform.createTransformedShape(shape);
        bounds = transformedShape.getBounds();
        double scale =  (bounds.getMaxX() > bounds.getMaxY()) ? columns / bounds.getMaxX() : rows / bounds.getMaxY();
        transform.setToIdentity();
        transform.scale(scale, scale);
        Shape finalShape = transform.createTransformedShape(transformedShape);
        boolean raster[][] = new boolean[rows][columns];
        IntStream.range(0, rows).forEach(i -> IntStream.range(0, columns).forEach(j -> raster[i][j] = finalShape.contains(j, i)));
        return trimRaster(raster);
    }

    private static boolean[][] trimRaster(boolean raster[][]) {
        int minRow = raster.length;
        int maxRow = 0;
        int minColumn = raster[0].length;
        int maxColumn = 0;

        for(int row = 0; row < raster.length; ++row) {
            for(int column = 0; column < raster[0].length; ++column) {
                if(raster[row][column]) {
                    if(minRow > row) {
                        minRow = row;
                    }
                    if(maxRow < row) {
                        maxRow = row;
                    }
                    if(minColumn > column) {
                        minColumn = column;
                    }
                    if(maxColumn < column) {
                        maxColumn = column;
                    }
                }
            }
        }
        final int minCol2 = minColumn;
        final int maxCol2 = maxColumn;
        final int minRow2 = minRow;
        boolean result[][] = new boolean[maxRow - minRow + 1][maxColumn - minColumn + 1];
        IntStream.range(minRow, maxRow + 1)
                .forEach(row -> IntStream.range(minCol2, maxCol2 + 1)
                .forEach(column -> result[row - minRow2][column - minCol2] = raster[row][column]));

//        for(int row = minRow; row <= maxRow; ++row) {
//            for(int column = minColumn; column <= maxColumn; ++column) {
//                result[row - minRow][column - minColumn] = raster[row][column];
//            }
//        }
        return result;
    }


}
