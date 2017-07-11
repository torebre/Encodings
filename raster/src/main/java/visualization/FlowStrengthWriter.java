package visualization;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FlowStrengthWriter implements RasterElementProcessor<FlowCell> {
    private final DecimalFormat decimalFormat;

    public FlowStrengthWriter() {
        decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        decimalFormat.setMaximumFractionDigits(2);
    }


    @Override
    public void processCell(FlowCell cell, int squareSize, ObservableList<Node> node, Rectangle rectangle) {
        Text text = new Text(cell.getRow() * squareSize, (cell.getRow() + 1) * squareSize,
                decimalFormat.format(cell.getFlow()));
        node.add(text);

    }
}
