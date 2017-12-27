package com.kjipo.raster.attraction;


import com.kjipo.prototype.AngleLine;
import com.kjipo.raster.segment.Pair;

public class AngleLineMoveOperationImpl implements AngleLineMoveOperation {
    private final int rowOffset;
    private final int columnOffset;
    private final double deltaLength;
    private final double deltaAngle;

    public AngleLineMoveOperationImpl(int rowOffset, int columnOffset, double deltaLength, double deltaAngle) {
        this.rowOffset = rowOffset;
        this.columnOffset = columnOffset;
        this.deltaLength = deltaLength;
        this.deltaAngle = deltaAngle;
    }


    @Override
    public void apply(AngleLine angleLine) {
        angleLine.setStartPair(Pair.of(angleLine.getStartPair().getRow() + rowOffset,
                angleLine.getStartPair().getColumn() + columnOffset));
        angleLine.setLength(angleLine.getLength() + deltaLength);
        angleLine.setAngleOffset(angleLine.getAngleOffset() + deltaAngle);
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public int getColumnOffset() {
        return columnOffset;
    }

    public double getDeltaLength() {
        return deltaLength;
    }

    public double getDeltaAngle() {
        return deltaAngle;
    }
}
