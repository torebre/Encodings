package visualization;

import com.kjipo.raster.AbstractCell;
import com.kjipo.raster.flow.FlowEncodedRaster;

public class FlowCell extends AbstractCell {

    private final FlowEncodedRaster flowEncodedRaster;

    public FlowCell(int row, int column, FlowEncodedRaster flowEncodedRaster) {
        super(row, column);
        this.flowEncodedRaster = flowEncodedRaster;
    }


    public double getFlow() {
        return flowEncodedRaster.getFlowRaster()[row][column].abs();
    }

    public double getArgument() {
        return flowEncodedRaster.getFlowRaster()[row][column].getArgument();
    }

}
