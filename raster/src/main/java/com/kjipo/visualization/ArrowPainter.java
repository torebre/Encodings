package com.kjipo.visualization;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

public class ArrowPainter implements RasterElementProcessor<FlowCell> {

    @Override
    public void processCell(FlowCell flowCell, int squareSize, ObservableList<Node> nodes, Rectangle rectangle) {
        if (flowCell.getFlow() > 0) {
            Polyline arrow = paintArrow(flowCell.getRow(), flowCell.getColumn(), squareSize,
                    flowCell.getArgument());
            arrow.setStroke(javafx.scene.paint.Color.BLUE);
            nodes.add(arrow);
        }
    }


    private static Polyline paintArrow(int row, int column, int squareSize, double angle) {
        Polyline arrow = new Polyline(-0.5, 0.0,
                0.5, 0.0,
                0.45, 0.05,
                0.45, -0.05,
                0.5, 0.0);
        arrow.setStrokeWidth(0.05);
        Affine affine = new Affine();

//        angle += Math.PI / 4;
        if (angle < 0) {
            angle += 2 * Math.PI;
        } else if (angle > 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }

//        LOG.info("Angle: {}", angle);

        affine.appendRotation(360 - (360 * angle) / (2 * Math.PI));

        arrow.translateXProperty().setValue(column * squareSize + squareSize / 2);
        arrow.translateYProperty().setValue(row * squareSize + squareSize / 2);

//        affine.appendTranslation(row * squareSize + squareSize / 2, column * squareSize + squareSize / 2);
        affine.appendScale(squareSize, squareSize);
        arrow.getTransforms().add(affine);
        return arrow;
    }
}
