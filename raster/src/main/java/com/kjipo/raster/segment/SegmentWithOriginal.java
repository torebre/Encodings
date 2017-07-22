package com.kjipo.raster.segment;

import java.util.List;

public class SegmentWithOriginal implements Segment {
    private final List<Pair> originalData;
    private final double rotationAngle;
    private final List<Pair> rotatedData;
    private final int squareSide;
    private final int numberOfRows;
    private final int numberOfColumns;


    public SegmentWithOriginal(List<Pair> originalData, List<Pair> rotatedData, double rotationAngle, int squareSide, int numberOfRows, int numberOfColumns) {
        this.originalData = originalData;
        this.rotationAngle = rotationAngle;
        this.rotatedData = rotatedData;
        this.squareSide = squareSide;
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
    }


    @Override
    public List<Pair> getPairs() {
        return rotatedData;
    }

    public List<Pair> getOriginalData() {
        return originalData;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public int getSquareSide() {
        return squareSide;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }
}
