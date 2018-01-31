package com.kjipo.visualization.segmentation;

import com.kjipo.visualization.RasterElementProcessor;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class ColorPainter implements RasterElementProcessor<ColorCell> {

    @Override
    public void processCell(ColorCell cell, int squareSize, ObservableList<Node> node, javafx.scene.shape.Rectangle rectangle) {
        rectangle.setFill(cell.getColor());
        if (cell.isSegmentData()) {
            rectangle.setFill(Color.GREEN);
        }
        if (cell.isPrototypeData()) {
            rectangle.setFill(Color.RED);
        }
    }
}
