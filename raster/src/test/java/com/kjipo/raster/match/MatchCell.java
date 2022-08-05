package com.kjipo.raster.match;

import com.kjipo.representation.segment.Pair;
import com.kjipo.visualization.CellType;

import java.util.List;

public class MatchCell implements CellType {
    private final int row;
    private final int column;

    private final List<Pair> segmentData;
    private final List<Pair> prototypeData;


    public MatchCell(int row, int column, List<Pair> segmentData, List<Pair> prototypeData) {
        this.row = row;
        this.column = column;
        this.segmentData = segmentData;
        this.prototypeData = prototypeData;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean isSegmentData() {
        return segmentData.contains(new Pair(row, column));
    }

    public boolean isPrototypeData() {
        return prototypeData.contains(new Pair(row, column));
    }
}
