package com.kjipo.segmentation;


import com.kjipo.raster.Cell;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KanjiSegmenter {

    private static final Logger LOG = LoggerFactory.getLogger(KanjiSegmenter.class);


    public static List<Segment> segmentKanji(Cell raster[][]) {
        List<Segment> segments = new ArrayList<>();
        boolean visitedCells[][] = new boolean[raster.length][raster[0].length];

        Pair startCell = getStartCell(visitedCells, raster);
        while (startCell != null) {
            Segment segment = determineSegment(startCell, raster, visitedCells);
            segments.add(segment);

            startCell = getStartCell(visitedCells, raster);
        }

        return segments;
    }


    private static Segment determineSegment(Pair startCell, Cell flowRaster[][], boolean visitedCells[][]) {
        List<Pair> pairs = new ArrayList<>();
        int currentRow = startCell.getRow();
        int currentColumn = startCell.getColumn();

        while (true) {
            pairs.add(new Pair(currentRow, currentColumn));
            visitedCells[currentRow][currentColumn] = true;
            FlowDirection flowDirection = flowRaster[currentRow][currentColumn].getFlowDirection();

            if (flowDirection == null) {
                break;
            }

            if(!EncodingUtilities.validCell(currentRow, currentColumn, flowDirection, flowRaster.length, flowRaster[0].length)) {
                break;
            }

            currentRow += flowDirection.getRowShift();
            currentColumn += flowDirection.getColumnShift();


            if (visitedCells[currentRow][currentColumn]) {
                break;
            }

        }

        return new SegmentImpl(pairs);
    }


    private static Pair getStartCell(boolean raster[][], Cell flowRaster[][]) {
        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (flowRaster[row][column].getFlowStrength() > 0 && !raster[row][column]) {
                    return new Pair(row, column);
                }
            }
        }
        return null;
    }


}
