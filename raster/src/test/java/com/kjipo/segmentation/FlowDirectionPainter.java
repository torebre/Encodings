package com.kjipo.segmentation;

import com.kjipo.visualization.RasterElementProcessor;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

public class FlowDirectionPainter implements RasterElementProcessor<FlowCell> {

    @Override
    public void processCell(FlowCell cell, int squareSize, ObservableList<Node> node, Rectangle rectangle) {
        if (cell.getCell().getFlowDirection() == null) {
            return;
        }
        double angle = cell.getCell().getFlowDirection().getAngleInRadians();

        Polyline arrow = new Polyline(-0.5, 0.0,
                0.5, 0.0,
                0.45, 0.05,
                0.45, -0.05,
                0.5, 0.0);
        arrow.setStrokeWidth(0.05);
        Affine affine = new Affine();

        if (angle < 0) {
            angle += 2 * Math.PI;
        } else if (angle > 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }


        affine.appendRotation(360 - (360 * angle) / (2 * Math.PI));
        arrow.translateXProperty().setValue(cell.getColumn() * squareSize + squareSize / 2);
        arrow.translateYProperty().setValue(cell.getRow() * squareSize + squareSize / 2);

        affine.appendScale(squareSize, squareSize);
        arrow.getTransforms().add(affine);

        node.add(arrow);
    }
}
