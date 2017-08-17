package com.kjipo.raster.match;

import com.kjipo.visualization.RasterElementProcessor;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PrototypePainter implements RasterElementProcessor<MatchCell> {

    @Override
    public void processCell(MatchCell cell, int squareSize, ObservableList<Node> node, Rectangle rectangle) {
        if (cell.isSegmentData()) {
            rectangle.setFill(Color.GREEN);
        }
        if (cell.isPrototypeData()) {
            rectangle.setFill(Color.RED);
        }
    }
}
