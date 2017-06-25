package visualization;

import com.kjipo.raster.flow.BooleanEncodingUtilities;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.shape.Line;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class RasterVisualizer2 {
    private static final int SLEEP_TIME_MS = 200;
    private static final int INITIAL_SQUARE_SIZE = 75;

    private static final Logger LOG = LoggerFactory.getLogger(visualization.RasterVisualizer.class);


    public static <T extends CellType> void showRasterFlow(RasterRun<T> rasterRun, Collection<RasterElementProcessor<T>> rasterElementProcessors)
            throws InterruptedException {
        JFXPanel panel = new JFXPanel();
        panel.setPreferredSize(new Dimension(rasterRun.getWidth() * INITIAL_SQUARE_SIZE,
                rasterRun.getHeight() * INITIAL_SQUARE_SIZE));
        JFrame frame = new JFrame();
        frame.add(panel);
        Group root = new Group();

        Platform.runLater(() -> {
            Scene scene = new Scene(root, rasterRun.getWidth() * INITIAL_SQUARE_SIZE,
                    rasterRun.getHeight() * INITIAL_SQUARE_SIZE, javafx.scene.paint.Color.BLACK);

            panel.setScene(scene);
        });


        frame.pack();
        frame.setVisible(true);

        int border[][] = BooleanEncodingUtilities.getBorder(rasterRun.getRawInput());

        while (rasterRun.hasNext()) {
            paintRaster(root, INITIAL_SQUARE_SIZE, rasterRun, border, rasterElementProcessors);
            Thread.sleep(SLEEP_TIME_MS);
        }
    }


    private static <T extends CellType> void paintRaster(Group pRoot,
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

            for (int row = 0; row < rasterRun.getHeight(); ++row) {
                for (int column = 0; column < rasterRun.getWidth(); ++column) {
                    javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(
                            column * squareSize,
                            row * squareSize,
                            squareSize,
                            squareSize);

                    for (RasterElementProcessor rasterElementProcessor : rasterElementProcessors) {
                        rasterElementProcessor.processCell(rasterRun.getCell(row, column), squareSize, children);
                    }


//                    javafx.scene.paint.Color color;

//                    if (flowRaster[row][column].abs() < 0.01) {
//                        // No flow at this point
//                        color = javafx.scene.paint.Color.gray(0.5, 0.5);
//                    } else {
//                        color = getColor(flowRaster[row][column], maxLength);
//                    }

//                    Text text = new Text(column * squareSize, (row + 1) * squareSize,
//                            decimalFormat.format(flowRaster[row][column].abs()));

//                    rectangle.setStroke(color);
//                    rectangle.setFill(color);

                    rectangles.getChildren().add(rectangle);
//                    rectangles.getChildren().add(text);

//                    // Paint arrow
//                    if (flowRaster[row][column].abs() > 0) {
//                        Polyline arrow = paintArrow(row, column, squareSize,
//                                flowRaster[row][column].getArgument());
//                        arrow.setStroke(javafx.scene.paint.Color.BLUE);
//                        rectangles.getChildren().add(arrow);
//                    }
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
                                column * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE,
                                column * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }

                    if ((border[row][column] & 2) == 2) {
                        javafx.scene.shape.Line borderLine = new Line(
                                column * INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE,
                                column * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }

                    if ((border[row][column] & 4) == 4) {
                        javafx.scene.shape.Line borderLine = new Line(
                                column * INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE,
                                column * INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }

                    if ((border[row][column] & 8) == 8) {
                        javafx.scene.shape.Line borderLine = new Line(
                                column * INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE,
                                column * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE,
                                row * INITIAL_SQUARE_SIZE + INITIAL_SQUARE_SIZE);
                        borderLine.setStroke(javafx.scene.paint.Color.YELLOW);
                        rectangles.getChildren().add(borderLine);
                    }
                }

            }
        }

    }


    private static javafx.scene.paint.Color getColor(Complex pComplex, double maxLength) {
//        double argument = pComplex.getArgument();
//        if(argument < 0) {
//            argument += 2 * Math.PI;
//        }

//        double brightness = Math.min(1.0, pComplex.abs());
//        return Color.hsb(360 * argument / (2 * Math.PI), 1, pComplex.abs() / maxLength);

        return javafx.scene.paint.Color.hsb(360 * (1 - (pComplex.abs() / maxLength)), 1, 1);
    }


    public static void main(String args[]) {


    }


}



