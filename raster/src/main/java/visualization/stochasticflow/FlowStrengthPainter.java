package visualization.stochasticflow;

import com.kjipo.raster.stochasticflow.StochasticCell;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import visualization.RasterElementProcessor;

public class FlowStrengthPainter implements RasterElementProcessor<StochasticCell> {
    @Override
    public void processCell(StochasticCell cell, int squareSize, ObservableList<Node> node, Rectangle rectangle) {
        if (cell.getFlowInCell() > 0) {
            rectangle.setFill(Color.GREEN);
        }

    }
}
