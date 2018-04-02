package com.kjipo.parser;

import com.kjipo.representation.EncodedKanji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
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
    public static final int NUMBER_OF_ROWS = 200;
    public static final int NUMBER_OF_COLUMNS = 200;

    private static final Logger logger = LoggerFactory.getLogger(FontFileParser.class);


    public static Collection<EncodedKanji> parseFontFile(Collection<Integer> unicodes, InputStream trueTypeFontData) throws IOException, FontFormatException {
        Font font = Font.createFont(Font.TRUETYPE_FONT, trueTypeFontData);
        FontRenderContext renderContext = new FontRenderContext(null, false, false);

        return unicodes.stream()
                .map(character -> {
                    GlyphVector glyphVector = font.createGlyphVector(renderContext, Character.toChars(character));
                    if (glyphVector.getNumGlyphs() > 1) {
                        logger.warn("Skipping character: " + character);
                        return null;
                    }
                    return new EncodedKanji(paintOnRaster(glyphVector, NUMBER_OF_ROWS, NUMBER_OF_COLUMNS), character);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Collection<EncodedKanji> parseFontFileUsingUnicodeInput(Collection<Integer> characters, InputStream trueTypeFontData) throws IOException, FontFormatException {
        return parseFontFileUsingUnicodeInput(characters, trueTypeFontData, NUMBER_OF_ROWS, NUMBER_OF_COLUMNS);
    }

    public static Collection<EncodedKanji> parseFontFileUsingUnicodeInput(Collection<Integer> unicode, InputStream trueTypeFontData, int numberOfRows, int numberOfColumns) throws IOException, FontFormatException {
        Font font = Font.createFont(Font.TRUETYPE_FONT, trueTypeFontData);
        FontRenderContext renderContext = new FontRenderContext(null, false, false);

        return unicode.stream()
                .map(character -> {
                    GlyphVector glyphVector = font.createGlyphVector(renderContext, new String(Character.toChars(character)));
                    if (glyphVector.getNumGlyphs() > 1) {
                        logger.warn("Skipping character: " + character);
                        return null;
                    }
                    return new EncodedKanji(paintOnRaster(glyphVector, NUMBER_OF_ROWS, NUMBER_OF_COLUMNS), character);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
        if(raster.length == 0) {
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
