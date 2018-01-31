package com.kjipo.raster.attraction;

import com.kjipo.prototype.LinePrototype;
import com.kjipo.raster.match.RotateSegment;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;

import java.util.List;
import java.util.stream.Collectors;

import static com.kjipo.raster.RasterConstants.SQUARE_SIDE;

public class MoveOperation implements LineMoveOperation {
    private final int rowOffset;
    private final int columnOffset;
    private final double rotation;
    private final int pivotRow;
    private final int pivotColumn;

    public MoveOperation(int rowOffset, int columnOffset, double rotation, int pivotRow, int pivotColumn) {
        this.rowOffset = rowOffset;
        this.columnOffset = columnOffset;
        this.rotation = rotation;
        this.pivotRow = pivotRow;
        this.pivotColumn = pivotColumn;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public int getColumnOffset() {
        return columnOffset;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return "MoveOperation{" +
                "rowOffset=" + rowOffset +
                ", columnOffset=" + columnOffset +
                ", rotation=" + rotation +
                '}';
    }

    public int getPivotRow() {
        return pivotRow;
    }

    public int getPivotColumn() {
        return pivotColumn;
    }

    @Override
    public Segment applyToLine(Segment linePrototype) {
        List<Pair> translatedCoordinates = linePrototype.getPairs().stream()
                .map(pair -> Pair.of(pair.getRow() + rowOffset, pair.getColumn() + columnOffset))
                .collect(Collectors.toList());
        List<Pair> rotatedCoordinates;


        if (Math.abs(rotation) > 0.001) {
            rotatedCoordinates = RotateSegment.rotateSegment(translatedCoordinates,
                    pivotRow,
                    pivotColumn,
                    // TODO Set proper constants
                    100,
                    100,
                    SQUARE_SIDE,
                    rotation);
        } else {
            rotatedCoordinates = translatedCoordinates;
        }

        return new SegmentImpl(rotatedCoordinates);
    }

}
