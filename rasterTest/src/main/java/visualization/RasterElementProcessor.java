package visualization;

import javafx.collections.ObservableList;
import javafx.scene.Node;

public interface RasterElementProcessor<T extends CellType> {


    void processCell(T cell, int squareSize, ObservableList<Node> node);


}
