package com.kjipo.segmentation;

import com.kjipo.raster.Cell;
import com.kjipo.visualization.CellType;

public class FlowCell implements CellType {
    private final int row;
    private final int column;
    private final Cell cell;

    public FlowCell(int row, int column, Cell cell) {
        this.row = row;
        this.column = column;
        this.cell = cell;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Cell getCell() {
        return cell;
    }

}
