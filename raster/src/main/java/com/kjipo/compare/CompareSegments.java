package com.kjipo.compare;

import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.representation.raster.FlowDirection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CompareSegments {


    public void compareSegments(List<Segment> segment1, List<Segment> segment2, int anchorSegment1, int anchorSegment2) {
        Segment anchor1 = segment1.get(anchorSegment1);
        Segment anchor2 = segment2.get(anchorSegment2);

        Segment nextPair1 = segment1.stream().filter(segment -> segment != anchor1).findFirst().orElse(null);
        Segment nextPair2 = segment2.stream().filter(segment -> segment != anchor2).findFirst().orElse(null);


    }


    List<FlowDirection> determineRelativePosition(Segment originSegment, Segment segment) {
        int minRowOrigin = originSegment.getPairs().stream().map(Pair::getRow).reduce((oldRow, newRow) -> newRow < oldRow ? newRow : oldRow).orElse(Integer.MIN_VALUE);
        int maxRowOrigin = originSegment.getPairs().stream().map(Pair::getRow).reduce((oldRow, newRow) -> newRow > oldRow ? newRow : oldRow).orElse(Integer.MAX_VALUE);

        int minColumnOrigin = originSegment.getPairs().stream().map(Pair::getColumn).reduce((oldColumn, newColumn) -> newColumn < oldColumn ? newColumn : oldColumn).orElse(Integer.MIN_VALUE);
        int maxColumnOrigin = originSegment.getPairs().stream().map(Pair::getColumn).reduce((oldColumn, newColumn) -> newColumn > oldColumn ? newColumn : oldColumn).orElse(Integer.MIN_VALUE);


        int minRowSegment = segment.getPairs().stream().map(Pair::getRow).reduce((oldRow, newRow) -> newRow < oldRow ? newRow : oldRow).orElse(Integer.MIN_VALUE);
        int maxRowSegment = segment.getPairs().stream().map(Pair::getRow).reduce((oldRow, newRow) -> newRow > oldRow ? newRow : oldRow).orElse(Integer.MAX_VALUE);

        int minColumnSegment = segment.getPairs().stream().map(Pair::getColumn).reduce((oldColumn, newColumn) -> newColumn < oldColumn ? newColumn : oldColumn).orElse(Integer.MIN_VALUE);
        int maxColumnSegment = segment.getPairs().stream().map(Pair::getColumn).reduce((oldColumn, newColumn) -> newColumn > oldColumn ? newColumn : oldColumn).orElse(Integer.MIN_VALUE);

        List<FlowDirection> result = new ArrayList<>();

        if (minRowSegment > maxRowOrigin && maxColumnSegment < minColumnOrigin) {
            result.add(FlowDirection.SOUTH_WEST);
        } else if (minRowSegment > maxRowOrigin && minColumnSegment > maxColumnOrigin) {
            result.add(FlowDirection.SOUTH_EAST);
        } else if (minRowSegment < maxRowOrigin) {
            result.add(FlowDirection.SOUTH);
        }

        // TODO Add other directions




        return result;
    }



}
