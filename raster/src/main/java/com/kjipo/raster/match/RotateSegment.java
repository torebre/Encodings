package com.kjipo.raster.match;


import com.kjipo.raster.attraction.MoveOperation;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.SegmentWithOriginal;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RotateSegment {
    private static final double angleIncrement = Math.PI / 4;

    private static final Logger LOG = LoggerFactory.getLogger(RotateSegment.class);


    public static SegmentWithOriginal rotateSegment45DegreesCounterClockwise(
            List<Pair> coordinates, int numberOfRows, int numberOfColumns, int squareSide) {
        Pair midpointCell = findMidpointCell(coordinates);
        int pivotRow = midpointCell.getRow();
        int pivotColumn = midpointCell.getColumn();

        rotate(coordinates, pivotRow, pivotColumn, numberOfRows, numberOfColumns, squareSide, angleIncrement);

        return new SegmentWithOriginal(coordinates,
                rotateSegment(coordinates,
                        numberOfRows,
                        numberOfColumns,
                        squareSide,
                        angleIncrement),
                angleIncrement,
                squareSide,
                numberOfRows,
                numberOfColumns,
                new MoveOperation(0, 0, angleIncrement, pivotRow, pivotColumn));
    }

    public static SegmentWithOriginal updateMatch(SegmentWithOriginal segment) {
        return new SegmentWithOriginal(segment.getOriginalData(),
                rotateSegment(segment.getOriginalData(),
                        segment.getNumberOfRows(),
                        segment.getNumberOfColumns(),
                        segment.getSquareSide(),
                        segment.getRotationAngle() + angleIncrement),
                segment.getRotationAngle() + angleIncrement,
                segment.getSquareSide(),
                segment.getNumberOfRows(),
                segment.getNumberOfColumns(),
                new MoveOperation(0, 0, 0, 0, 0));
    }

    private static List<Pair> rotateSegment(List<Pair> coordinates, int rows, int columns, int squareSide, double angle) {
        Pair midpointCell = findMidpointCell(coordinates);
        int pivotRow = midpointCell.getRow();
        int pivotColumn = midpointCell.getColumn();

        return rotate(coordinates, pivotRow, pivotColumn, rows, columns, squareSide, angle);
    }

    public static List<Pair> rotate(List<Pair> coordinates, int pivotRow, int pivotColumn, int rows, int columns, int squareSide, double angle) {
        double cellCenter = (double) squareSide / 2;
        double xCenter = squareSide * pivotColumn + cellCenter;
        double yCenter = squareSide * pivotRow + cellCenter;

        List<Pair> newResults = new ArrayList<>();

        for (Pair coordinate : coordinates) {
            int row = coordinate.getRow();
            int column = coordinate.getColumn();

            double columnCenter = squareSide * column + cellCenter;
            double rowCenter = squareSide * row + cellCenter;

            Complex complex = new Complex(columnCenter - xCenter, rowCenter - yCenter);
            Complex rotatedCellCoordinates = ComplexUtils.polar2Complex(complex.abs(), complex.getArgument() + angle);

            Pair newCoordinates = determineCellFromCoordinates(rotatedCellCoordinates.getReal() + xCenter,
                    rotatedCellCoordinates.getImaginary() + yCenter, rows, columns, squareSide);

            if (newCoordinates != null) {

                LOG.info("X center: {}. Y center: {}", xCenter, yCenter);

                LOG.info("New coordinates. Row: {}. Column: {}", rotatedCellCoordinates.getReal() + xCenter,
                        rotatedCellCoordinates.getImaginary() + yCenter);

                LOG.info("New row: {}. New column: {}", newCoordinates.getRow(), newCoordinates.getColumn());

                newResults.add(newCoordinates);
            }
        }

        return newResults;
    }


    private static Pair determineCellFromCoordinates(double xCoordinate, double yCoordinate, int rows, int columns, int squareSide) {
        if (xCoordinate < 0
                || yCoordinate < 0
                || xCoordinate > rows * squareSide
                || yCoordinate > columns * squareSide) {
            return null;
        }

        int column = (int) Math.floor(xCoordinate / squareSide);
        int row = (int) Math.floor(yCoordinate / squareSide);

        return new Pair(row, column);
    }


    private static Pair findMidpointCell(Collection<Pair> coordinates) {
        int numberOfPoints = coordinates.size();
        int pivotRow = coordinates.stream().map(Pair::getRow).sorted().skip(numberOfPoints / 2).findFirst().orElse(0);
        int pivotColumn = coordinates.stream().map(Pair::getColumn).sorted().skip(numberOfPoints / 2).findFirst().orElse(0);

//        LOG.info("Midpoint. Row: {}. Column: {}", pivotRow, pivotColumn);

        return new Pair(pivotRow, pivotColumn);
    }


}
