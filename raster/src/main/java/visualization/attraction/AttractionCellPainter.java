package visualization.attraction;

import com.kjipo.raster.attraction.AttractionCell;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import visualization.RasterElementProcessor;

public class AttractionCellPainter implements RasterElementProcessor<AttractionCell> {
    @Override
    public void processCell(AttractionCell cell, int squareSize, ObservableList<Node> node, Rectangle rectangle) {
        if(cell.isFilled()) {
            rectangle.setFill(Color.GREEN);
        }
        else if(cell.containsPrototypeCell()) {
            rectangle.setFill(Color.WHITE);
        }

    }
}
