package com.kjipo.visualization;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

public interface RasterElementProcessor<T extends CellType> {


    void processCell(T cell, int squareSize, ObservableList<Node> node, Rectangle rectangle);


}
