package com.kjipo.visualization;

import com.kjipo.raster.flow.BooleanEncodingUtilities;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

import static com.kjipo.raster.RasterConstants.SQUARE_SIDE;

public class RasterVisualizer2 {
    private static final int SLEEP_TIME_MS = 500;

    private static final Logger LOG = LoggerFactory.getLogger(RasterVisualizer2.class);


    public static <T extends CellType> void showRasterFlow(
            RasterRun<T> rasterRun,
            Collection<RasterElementProcessor<T>> rasterElementProcessors)
            throws InterruptedException {
        JFXPanel panel = new JFXPanel();
        panel.setPreferredSize(new Dimension(rasterRun.getColumns() * SQUARE_SIDE,
                rasterRun.getRows() * SQUARE_SIDE));
        JFrame frame = new JFrame();
        frame.add(panel);
        Group root = new Group();

        Platform.runLater(() -> {
            Scene scene = new Scene(root, rasterRun.getColumns() * SQUARE_SIDE,
                    rasterRun.getRows() * SQUARE_SIDE, javafx.scene.paint.Color.BLACK);

            panel.setScene(scene);
        });

        frame.pack();
        frame.setVisible(true);

        int border[][] = BooleanEncodingUtilities.getBorder(rasterRun.getRawInput());

        int counter = 0;
        while (rasterRun.hasNext()) {

            LOG.info("Painting element: " + (counter++));

            paintRaster(root, SQUARE_SIDE, rasterRun, border, rasterElementProcessors);
            rasterRun.next();

            Thread.sleep(SLEEP_TIME_MS);
        }
    }


    public static void paintRaster(boolean raster[][]) {
        JFXPanel panel = new JFXPanel();
        panel.setPreferredSize(new Dimension(raster[0].length * SQUARE_SIDE,
                raster.length * SQUARE_SIDE));
        JFrame frame = new JFrame();
        frame.add(panel);
        Group root = new Group();

        Platform.runLater(() -> {
            Scene scene = new Scene(root, raster[0].length * SQUARE_SIDE,
                    raster.length * SQUARE_SIDE, javafx.scene.paint.Color.BLACK);

            panel.setScene(scene);
        });

        frame.pack();
        frame.setVisible(true);

        int border[][] = BooleanEncodingUtilities.getBorder(raster);

        Platform.runLater(() -> {
            root.getChildren().clear();
            Group rectangles = new Group();
            ObservableList<Node> children = rectangles.getChildren();

            for (int row = 0; row < raster.length; ++row) {
                for (int column = 0; column < raster[0].length; ++column) {
                    javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(
                            column * SQUARE_SIDE,
                            row * SQUARE_SIDE,
                            SQUARE_SIDE,
                            SQUARE_SIDE);

                    children.add(rectangle);
                }
            }

            paintBorder(rectangles, border);

            root.getChildren().add(rectangles);
        });

    }


    public static void paintRaster(Color colorRaster[][]) {
        JFXPanel panel = new JFXPanel();
        panel.setPreferredSize(new Dimension(colorRaster[0].length * SQUARE_SIDE,
                colorRaster.length * SQUARE_SIDE));
        JFrame frame = new JFrame();
        frame.add(panel);
        Group root = new Group();

        Platform.runLater(() -> {
            Scene scene = new Scene(root, colorRaster[0].length * SQUARE_SIDE,
                    colorRaster.length * SQUARE_SIDE, javafx.scene.paint.Color.BLACK);
            panel.setScene(scene);
        });

        frame.pack();
        frame.setVisible(true);

        Platform.runLater(() -> {
            root.getChildren().clear();
            Group rectangles = new Group();
            ObservableList<Node> children = rectangles.getChildren();

            for (int row = 0; row < colorRaster.length; ++row) {
                for (int column = 0; column < colorRaster[0].length; ++column) {
                    javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(
                            column * SQUARE_SIDE,
                            row * SQUARE_SIDE,
                            SQUARE_SIDE,
                            SQUARE_SIDE);

                    rectangle.setFill(colorRaster[row][column]);
                    children.add(rectangle);
                }
            }

            root.getChildren().add(rectangles);
        });

    }


    private static <T extends CellType> void paintRaster(
            Group pRoot,
            int squareSize,
            RasterRun<T> rasterRun,
            int border[][],
            Collection<RasterElementProcessor<T>> rasterElementProcessors) {
        if (squareSize < 1) {
            return;
        }

        Platform.runLater(() -> {
            pRoot.getChildren().clear();
            Group rectangles = new Group();
            ObservableList<Node> children = rectangles.getChildren();

            for (int row = 0; row < rasterRun.getRows(); ++row) {
                for (int column = 0; column < rasterRun.getColumns(); ++column) {
                    javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(
                            column * squareSize,
                            row * squareSize,
                            squareSize,
                            squareSize);

                    children.add(rectangle);

                    for (RasterElementProcessor rasterElementProcessor : rasterElementProcessors) {
                        rasterElementProcessor.processCell(rasterRun.getCell(row, column), squareSize, children, rectangle);
                    }

                }
            }

            paintBorder(rectangles, border);

            pRoot.getChildren().add(rectangles);
        });

    }

    private static void paintBorder(Group rectangles, int border[][]) {
        for (int row = 0; row < border.length; ++row) {
            for (int column = 0; column < border[0].length; ++column) {
                if (border[row][column] != 0) {
                    if ((border[row][column] & 1) == 1) {
                        javafx.scene.shape.Line borderLine = new Line(
                                column * SQUARE_SIDE + SQUARE_SIDE,
                                row * SQUARE_SIDE,
                                column * SQUARE_SIDE + SQUARE_SIDE,
                                row * SQUARE_SIDE + SQUARE_SIDE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }

                    if ((border[row][column] & 2) == 2) {
                        javafx.scene.shape.Line borderLine = new Line(
                                column * SQUARE_SIDE,
                                row * SQUARE_SIDE,
                                column * SQUARE_SIDE + SQUARE_SIDE,
                                row * SQUARE_SIDE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }

                    if ((border[row][column] & 4) == 4) {
                        javafx.scene.shape.Line borderLine = new Line(
                                column * SQUARE_SIDE,
                                row * SQUARE_SIDE,
                                column * SQUARE_SIDE,
                                row * SQUARE_SIDE + SQUARE_SIDE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }

                    if ((border[row][column] & 8) == 8) {
                        javafx.scene.shape.Line borderLine = new Line(
                                column * SQUARE_SIDE,
                                row * SQUARE_SIDE + SQUARE_SIDE,
                                column * SQUARE_SIDE + SQUARE_SIDE,
                                row * SQUARE_SIDE + SQUARE_SIDE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }
                }

            }
        }

    }


}



