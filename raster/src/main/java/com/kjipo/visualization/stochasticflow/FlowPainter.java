package com.kjipo.visualization.stochasticflow;

import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.stochasticflow.StochasticCell;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.visualization.RasterElementProcessor;

public class FlowPainter implements RasterElementProcessor<StochasticCell> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPainter.class);

    @Override
    public void processCell(StochasticCell cell, int squareSize, ObservableList<Node> nodes, Rectangle rectangle) {
        FlowDirection flowDirection = cell.getFlowDirectionInCell();

        if(flowDirection == null) {
            return;
        }

        Polyline arrow = paintArrow(cell.getRow(), cell.getColumn(), squareSize, flowDirection.getAngleInRadians());
        arrow.setStroke(Color.BLUE);
        nodes.add(arrow);
    }


    private static Polyline paintArrow(int row, int column, int squareSize, double angle) {
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
        arrow.translateXProperty().setValue(column * squareSize + squareSize / 2);
        arrow.translateYProperty().setValue(row * squareSize + squareSize / 2);

        affine.appendScale(squareSize, squareSize);
        arrow.getTransforms().add(affine);
        return arrow;
    }


}
