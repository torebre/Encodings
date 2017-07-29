package com.kjipo.raster.attraction;

public class MoveOperation {
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
}
