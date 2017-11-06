package com.kjipo.visualization.segmentation;

import com.kjipo.raster.segment.Pair;
import com.kjipo.visualization.CellType;
import javafx.scene.paint.Color;

import java.util.List;

public class ColorCell implements CellType {
    private final int row;
    private final int column;
    private final Color color;
    private final List<Pair> segmentData;
    private final List<Pair> prototypeData;


    public ColorCell(int row, int column, Color color, List<Pair> segmentData, List<Pair> prototypeData) {
        this.row = row;
        this.column = column;
        this.color = color;
        this.segmentData = segmentData;
        this.prototypeData = prototypeData;
    }

    public Color getColor() {
        return color;
    }

    public boolean isSegmentData() {
        return segmentData.contains(new Pair(row, column));
    }

    public boolean isPrototypeData() {
        return prototypeData.contains(new Pair(row, column));
    }
}
