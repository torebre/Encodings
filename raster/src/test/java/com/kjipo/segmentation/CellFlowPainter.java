package com.kjipo.segmentation;

import com.kjipo.visualization.RasterElementProcessor;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class CellFlowPainter implements RasterElementProcessor<FlowCell> {

    @Override
    public void processCell(FlowCell cell, int squareSize, ObservableList<Node> node, javafx.scene.shape.Rectangle rectangle) {
        if (cell.getCell().getFlowStrength() > 0) {
            rectangle.setFill(Color.RED);
        }
    }
}
