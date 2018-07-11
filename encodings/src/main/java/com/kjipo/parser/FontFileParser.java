package com.kjipo.parser;

import com.kjipo.representation.EncodedKanji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FontFileParser {
    public static final int NUMBER_OF_ROWS = 100;
    public static final int NUMBER_OF_COLUMNS = 100;

    private static final Logger logger = LoggerFactory.getLogger(FontFileParser.class);


    public static Collection<EncodedKanji> parseFontFile(Collection<Integer> unicodes, InputStream trueTypeFontData, int numberOfRows, int numberOfColumns) throws IOException, FontFormatException {
        Font font = Font.createFont(Font.TRUETYPE_FONT, trueTypeFontData);
        FontRenderContext fontRenderContext = getFontRenderContext();

        return unicodes.stream()
                .map(character -> {
                    GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, Character.toChars(character));
                    if (glyphVector.getNumGlyphs() > 1) {
                        logger.warn("Skipping character: " + character);
                        return null;
                    }
                    return new EncodedKanji(paintOnRaster(glyphVector, numberOfRows, numberOfColumns), character);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Collection<EncodedKanji> parseFontFileUsingUnicodeInput(Collection<Integer> unicodes, InputStream trueTypeFontData, int numberOfRows, int numberOfColumns) throws IOException, FontFormatException {
        Font font = Font.createFont(Font.TRUETYPE_FONT, trueTypeFontData);
        FontRenderContext fontRenderContext = getFontRenderContext();

        return unicodes.stream()
                .map(unicode -> createEncodedKanji(unicode, font, fontRenderContext, numberOfRows, numberOfColumns))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static EncodedKanji createEncodedKanji(int unicode, Font font, FontRenderContext fontRenderContext, int numberOfRows, int numberOfColumns) {
        GlyphVector glyphVector = createGlyphVector(unicode, font, fontRenderContext);
        if (glyphVector.getNumGlyphs() > 1) {
            logger.warn("Skipping character: " + unicode);
            return null;
        }
        return new EncodedKanji(paintOnRaster(glyphVector, numberOfRows, numberOfColumns), unicode);
    }


    public static GlyphVector createGlyphVector(int unicode, Font font, FontRenderContext fontRenderContext) {
        return font.createGlyphVector(fontRenderContext, new String(Character.toChars(unicode)));
    }

    public static FontRenderContext getFontRenderContext() {
        // If the render context is not scaled up, it can cause filled pixels to be skipped
        AffineTransform affineTransform = AffineTransform.getScaleInstance(10.0, 10.0);
        return new FontRenderContext(affineTransform, true, true);
    }

    public static boolean[][] setupRaster(GlyphVector glyphVector, int height, int width) throws IllegalArgumentException {
        assertOnlyOneGlyphInGlyphVector(glyphVector);
        return paintOnRaster(glyphVector, height, width);
    }

    public static boolean[][] paintOnRaster(GlyphVector glyphVector, int pRows, int pColumns) {
        assertOnlyOneGlyphInGlyphVector(glyphVector);
        for (int i = 0; i < glyphVector.getNumGlyphs(); ++i) {
            Shape shape = glyphVector.getGlyphOutline(i);
            logger.debug("Bounds {}, {}, {}, {}",
                    shape.getBounds().getMinX(), shape.getBounds().getMaxX(),
                    shape.getBounds().getMinY(), shape.getBounds().getMaxY());
        }
        return paintOnRaster(glyphVector.getGlyphOutline(0), pRows, pColumns);
    }

    private static void assertOnlyOneGlyphInGlyphVector(GlyphVector glyphVector) throws IllegalArgumentException {
        if (glyphVector.getNumGlyphs() > 1) {
            throw new IllegalArgumentException("Only one glyph supported for now");
        }
    }

    public static boolean[][] paintOnRaster(Shape shape, int rows, int columns) {
        Rectangle bounds = shape.getBounds();
        AffineTransform transform = new AffineTransform();
        transform.translate(-bounds.getMinX(), -bounds.getMinY());
        Shape transformedShape = transform.createTransformedShape(shape);
        bounds = transformedShape.getBounds();
        double scale = (bounds.getMaxX() > bounds.getMaxY()) ? columns / bounds.getMaxX() : rows / bounds.getMaxY();
        transform.setToIdentity();
        transform.scale(scale, scale);
        Shape finalShape = transform.createTransformedShape(transformedShape);
        boolean raster[][] = new boolean[rows][columns];
        IntStream.range(0, rows).forEach(i -> IntStream.range(0, columns).forEach(j -> raster[i][j] = finalShape.contains(j, i)));
        return scaleRaster(trimRaster(raster), rows, columns);
    }

    private static boolean[][] trimRaster(boolean raster[][]) {
        int minRow = raster.length;
        int maxRow = 0;
        int minColumn = raster[0].length;
        int maxColumn = 0;

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (raster[row][column]) {
                    if (minRow > row) {
                        minRow = row;
                    }
                    if (maxRow < row) {
                        maxRow = row;
                    }
                    if (minColumn > column) {
                        minColumn = column;
                    }
                    if (maxColumn < column) {
                        maxColumn = column;
                    }
                }
            }
        }
        final int minCol2 = minColumn;
        final int maxCol2 = maxColumn;
        final int minRow2 = minRow;

        if (maxRow < minRow || maxColumn < minColumn) {
            logger.error("maxRow: " + maxRow
                    + ", minRow: " + minRow
                    + ", maxColumn: " + maxColumn
                    + ", minColumn: " + minColumn);
            return new boolean[0][0];
        }

        boolean result[][] = new boolean[maxRow - minRow + 1][maxColumn - minColumn + 1];
        IntStream.range(minRow, maxRow + 1)
                .forEach(row -> IntStream.range(minCol2, maxCol2 + 1)
                        .forEach(column -> result[row - minRow2][column - minCol2] = raster[row][column]));
        return result;
    }


    private static boolean[][] scaleRaster(boolean raster[][], int newNumberOfRows, int newNumberOfColumns) {
        if (raster.length == 0) {
            logger.error("Invalid dimensions for raster: {}", raster.length);
            return new boolean[newNumberOfRows][newNumberOfColumns];
        }

        double scaleRow = raster.length / (double) newNumberOfRows;
        double scaleColumn = raster[0].length / (double) newNumberOfColumns;
        boolean result[][] = new boolean[newNumberOfRows][newNumberOfColumns];

        for (int row = 0; row < newNumberOfRows; ++row) {
            for (int column = 0; column < newNumberOfColumns; ++column) {
                int lookupRow = (int) Math.floor(row * scaleRow);
                int lookupColumn = (int) Math.floor(column * scaleColumn);
                result[row][column] = raster[lookupRow][lookupColumn];
            }
        }

        return result;
    }

}
