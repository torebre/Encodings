package com.kjipo.setupUtilities;/**
 * Created by student on 7/10/14.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;


public class RasterVisualizer extends Application {
    private Complex currentRaster[][];
    private Group root;

    private static RasterVisualizer instance;

    private static final Logger LOG = LoggerFactory.getLogger(RasterVisualizer.class);



    public RasterVisualizer() {

    }


    public static RasterVisualizer getInstance() {
        return instance;
    }


    @Override
    public void init() {
        instance = this;

        // TODO Make lengths variable
        // TODO Added data just for testing
        currentRaster = new Complex[100][100];
        IntStream.range(0, 100).forEach(i -> IntStream.range(0, 100).forEach(j -> currentRaster[i][j] = new Complex((i + j) % 2, 0)));


    }


    @Override
    public void start(Stage primaryStage) {
        root = new Group();

        // TODO Need to update dimensions if they change

        Scene scene = new Scene(root, currentRaster.length * 10, currentRaster[0].length * 10, Color.BLACK);
        paintRaster();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void paintRaster() {
        root.getChildren().clear();
        Group rectangles = new Group();
        for(int row = 0; row < currentRaster.length; ++row) {
            for(int column = 0; column < currentRaster[0].length; ++column) {
                double abs = currentRaster[row][column].abs();

                if(abs > 0) {
                    Rectangle rectangle = new Rectangle(row * 10, column * 10, 10, 10);
                    rectangle.setStroke(Color.web("white"));
                    rectangle.setFill(Color.web("white"));
                    rectangles.getChildren().add(rectangle);
                }

            }
        }
        root.getChildren().add(rectangles);
    }


    public void showRaster(Complex pRaster[][]) {
        if(Platform.isFxApplicationThread()) {
            showRaster0(pRaster);
        }
        else {
            Platform.runLater(() -> {
                showRaster0(pRaster);
            });
        }
    }

    private void showRaster0(Complex pRaster[][]) {

        LOG.info("Painting raster");

        currentRaster = pRaster;
        paintRaster();
    }



    public static void main(String[] args) {
        launch(args);
    }



}
