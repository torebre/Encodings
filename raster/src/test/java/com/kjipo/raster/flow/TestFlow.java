package com.kjipo.raster.flow;


import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import org.apache.commons.math3.complex.Complex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;



public class TestFlow {
    private static Font testFont;
    private static String testCharacter;

    private static final Logger LOG = LoggerFactory.getLogger(TestFlow.class);


    @BeforeClass
    public static void beforeClass() throws IOException, FontFormatException {
        InputStream fontStream = TestFlow.class.getResourceAsStream("/font/kochi-mincho-subst.ttf");
        testFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        fontStream.close();
        InputStreamReader input = new InputStreamReader(
                new BufferedInputStream(new FileInputStream(new File("/home/student/edict/edict2"))), "EUC-JP");
        BufferedReader reader = new BufferedReader(input);
        testCharacter = reader.readLine().substring(0, 1);
    }

    private GlyphVector getGlyphForCharacter(String pTestCharacter) {
        FontRenderContext renderContext = new FontRenderContext(null, false, false);
        return testFont.createGlyphVector(renderContext, pTestCharacter);
    }


    @Test
    public void testFlow() throws InterruptedException {
//        BooleanEncoding booleanEncoding = new BooleanEncoding(RasterUtilities.setupRaster(getGlyphForCharacter(testCharacter)));
        boolean rawData[][] = BooleanEncodingTestData.getTestRaster4(5, 20);
        BooleanEncoding booleanEncoding = new BooleanEncoding(rawData,
                BooleanEncodingUtilities.setupFlowRaster(rawData));
        Complex raster[][] = booleanEncoding.getFlowRaster();

        JFXPanel panel = new JFXPanel();
        JFrame frame = new JFrame();
        frame.add(panel);

        Group root = new Group();

        Platform.runLater(() -> {
            Scene scene = new Scene(root, raster.length, raster[0].length, Color.BLACK);
            panel.setScene(scene);
        });

        AtomicReference<Integer> initialSquareSize = new AtomicReference<>(75);

        paintRaster(root, booleanEncoding, 1, initialSquareSize.get());
        frame.setSize(new Dimension(raster.length * initialSquareSize.get(), raster[0].length * initialSquareSize.get()));
        frame.setVisible(true);

        frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                            int width = frame.getWidth() / raster[0].length;
                            int height = frame.getHeight() / raster.length;
                            initialSquareSize.set(Math.min(height, width));
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });

        BooleanEncodingIterator.placeSource(booleanEncoding);

        boolean firstStep = true;

        while(true) {
            Thread.sleep(1000);

            BooleanEncodingIterator.iterate(booleanEncoding);

//            booleanEncoding.iterate();
            firstStep = false;

            double maxLength = BooleanEncodingUtilities.getMaxLength(booleanEncoding);
            paintRaster(root, booleanEncoding, maxLength, initialSquareSize.get());
        }

    }


    private void paintRaster(Group pRoot, BooleanEncoding pEncoding, double maxLength, int squareSize) {
        if(squareSize < 1) {
            return;
        }
        Platform.runLater(() -> {
            pRoot.getChildren().clear();
            Group rectangles = new Group();

            Complex flowRaster[][] = pEncoding.getFlowRaster();

            for (int row = 0; row < flowRaster.length; ++row) {
                for (int column = 0; column < flowRaster[0].length; ++column) {
                    Rectangle rectangle = new Rectangle(column * squareSize, row * squareSize, squareSize, squareSize);

                    Color color;
                    if (flowRaster[row][column].abs() == 0) {
                        // No flow at this point
                        color = Color.gray(0.5, 0.5);
                    } else {
                        color = getColor(flowRaster[row][column], maxLength);
                    }

                    if(BooleanEncodingUtilities.isSourceAtPoint(row, column, pEncoding)) {
                        color = Color.RED;
                    }

                    if(BooleanEncodingUtilities.isSourceAtPoint(row, column, pEncoding)) {
                        color = color.brighter();
                    }

                    rectangle.setStroke(color);
                    rectangle.setFill(color);

                    rectangles.getChildren().add(rectangle);

                    if(flowRaster[row][column].abs() > 0) {
                        Polyline arrow = paintArrow(row, column, squareSize, flowRaster[row][column].getArgument());
                        arrow.setStroke(Color.BLUE);

//                    arrow.setTranslateX(row * squareSize);
//                    arrow.setTranslateY(column * squareSize);

//                    LOG.info("Arrow {}, {}: ", row, column);
//                    for(double d : arrow.getPoints()) {
//                        LOG.info("{}, ", d);
//                    }

                        rectangles.getChildren().add(arrow);
                    }
                }
            }
            pRoot.getChildren().add(rectangles);
        });


    }


    private Color getColor(Complex pComplex, double maxLength) {
//        double argument = pComplex.getArgument();
//        if(argument < 0) {
//            argument += 2 * Math.PI;
//        }

//        double brightness = Math.min(1.0, pComplex.abs());
//        return Color.hsb(360 * argument / (2 * Math.PI), 1, pComplex.abs() / maxLength);

        return Color.hsb(360 * (1 - (pComplex.abs() / maxLength)), 1, 1);
    }


    private Polyline paintArrow(int row, int column, int squareSize, double angle) {
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
