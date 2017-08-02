package com.kjipo.visualization;


import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.raster.flow.BooleanEncoding;
import com.kjipo.raster.flow.BooleanEncodingUtilities;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;



public class RasterVisualizer {

    private static final Logger LOG = LoggerFactory.getLogger(RasterVisualizer.class);



    public static void showRasterFlow(List<BooleanEncoding> flowRun) throws InterruptedException {
        showRasterFlow(flowRun, null);
    }

    public static void showRasterFlow(List<BooleanEncoding> flowRun, boolean originalRaster[][])
            throws InterruptedException {
        boolean raster[][] = flowRun.get(0).getRaster();
        int initialSquareSize = 75;

        LOG.info("Raster: {}, {}", raster.length, raster[0].length);

        JFXPanel panel = new JFXPanel();
        panel.setPreferredSize(new Dimension(raster[0].length * initialSquareSize,
                raster.length * initialSquareSize));
        JFrame frame = new JFrame();
        frame.add(panel);
        Group root = new Group();

        Platform.runLater(() -> {
            Scene scene = new Scene(root, raster[0].length * initialSquareSize,
                    raster.length * initialSquareSize, Color.BLACK);

            panel.setScene(scene);
        });

        double maxLengthForRun = BooleanEncodingUtilities.getMaxLength(flowRun);

//        frame.setSize(new Dimension(com.kjipo.raster.length * initialSquareSize,
//                com.kjipo.raster[0].length * initialSquareSize));

        frame.pack();
        frame.setVisible(true);

        int border[][] = BooleanEncodingUtilities.getBorder(originalRaster);


        for(BooleanEncoding booleanEncoding : flowRun) {

            Thread.sleep(200);

            paintRaster(root, booleanEncoding, maxLengthForRun, initialSquareSize, originalRaster, border);

        }



//        frame.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                int width = frame.getColumns() / com.kjipo.raster[0].length;
//                int height = frame.getRows() / com.kjipo.raster.length;
//                initialSquareSize.set(Math.min(height, width));
//            }
//        });

//        BooleanEncodingIterator.placeSource(booleanEncoding);

//        boolean firstStep = true;

    }



    private static void paintRaster(Group pRoot, BooleanEncoding pEncoding, double maxLength, int squareSize,
                                    boolean originalRaster[][], int border[][]) {
        if(squareSize < 1) {
            return;
        }

        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.setMaximumFractionDigits(2);

        Platform.runLater(() -> {
            pRoot.getChildren().clear();
            Group rectangles = new Group();

            Complex flowRaster[][] = pEncoding.getFlowRaster();

            for (int row = 0; row < flowRaster.length; ++row) {
                for (int column = 0; column < flowRaster[0].length; ++column) {
                    javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(
                            column * squareSize,
                            row * squareSize,
                            squareSize,
                            squareSize);


                    Color color;
                    if (flowRaster[row][column].abs() < 0.01) {
                        // No flow at this point
                        color = Color.gray(0.5, 0.5);
                    } else {
                        color = getColor(flowRaster[row][column], maxLength);
                    }


                    Text text = new Text(column * squareSize, (row + 1) * squareSize,
                            decimalFormat.format(flowRaster[row][column].abs()));




//                    if(BooleanEncodingUtilities.isSourceAtPoint(row, column, pEncoding)) {
//                        color = Color.RED;
//                    }
//
//                    if(BooleanEncodingUtilities.isSourceAtPoint(row, column, pEncoding)) {
//                        color = color.brighter();
//                    }

//                    if(originalRaster != null && originalRaster[row][column]) {
//                        color = color.brighter();
//                    }

                    rectangle.setStroke(color);
                    rectangle.setFill(color);

                    rectangles.getChildren().add(rectangle);
                    rectangles.getChildren().add(text);

                    // TODO The border does not change. It is not necessary to compute it like this in every iteration
                    if(border[row][column] != 0) {
                        if ((border[row][column] & 1) == 1) {
                            javafx.scene.shape.Line borderLine = new Line(
                                    column * squareSize + squareSize,
                                    row * squareSize,
                                    column * squareSize + squareSize,
                                    row * squareSize + squareSize);
                            borderLine.setStroke(Color.YELLOW);
                            rectangles.getChildren().add(borderLine);
                        }

                        if ((border[row][column] & 2) == 2) {
                            javafx.scene.shape.Line borderLine = new Line(
                                    column * squareSize,
                                    row * squareSize,
                                    column * squareSize + squareSize,
                                    row * squareSize);
                            borderLine.setStroke(Color.YELLOW);
                            rectangles.getChildren().add(borderLine);
                        }

                        if ((border[row][column] & 4) == 4) {
                            javafx.scene.shape.Line borderLine = new Line(
                                    column * squareSize,
                                    row * squareSize,
                                    column * squareSize,
                                    row * squareSize + squareSize);
                            borderLine.setStroke(Color.YELLOW);
                            rectangles.getChildren().add(borderLine);
                        }

                        if ((border[row][column] & 8) == 8) {
                            javafx.scene.shape.Line borderLine = new Line(
                                    column * squareSize,
                                    row * squareSize + squareSize,
                                    column * squareSize + squareSize,
                                    row * squareSize + squareSize);
                            borderLine.setStroke(Color.YELLOW);
                            rectangles.getChildren().add(borderLine);
                        }
                    }

                    // Paint arrow
                    if(flowRaster[row][column].abs() > 0) {
                        Polyline arrow = paintArrow(row, column, squareSize,
                                flowRaster[row][column].getArgument());
                        arrow.setStroke(Color.BLUE);
                        rectangles.getChildren().add(arrow);
                    }
                }
            }
            pRoot.getChildren().add(rectangles);
        });


    }


    private static Color getColor(Complex pComplex, double maxLength) {
//        double argument = pComplex.getArgument();
//        if(argument < 0) {
//            argument += 2 * Math.PI;
//        }

//        double brightness = Math.min(1.0, pComplex.abs());
//        return Color.hsb(360 * argument / (2 * Math.PI), 1, pComplex.abs() / maxLength);

        return Color.hsb(360 * (1 - (pComplex.abs() / maxLength)), 1, 1);
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
        if(angle < 0) {
            angle += 2 * Math.PI;
        }
        else if(angle > 2 * Math.PI) {
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
