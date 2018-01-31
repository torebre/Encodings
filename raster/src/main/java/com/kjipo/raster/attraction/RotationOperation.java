package com.kjipo.raster.attraction;


import com.kjipo.raster.match.RotateSegment;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;

import java.util.List;
import java.util.stream.Collectors;

import static com.kjipo.raster.RasterConstants.SQUARE_SIDE;

public class RotationOperation implements LineMoveOperation {
    private final double rotation;


    public RotationOperation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }


    @Override
    public Segment applyToLine(Segment linePrototype) {
//        Pair startPair = linePrototype.getPairs().get(0);
//        List<Pair> translatedCoordinates = linePrototype.getPairs().stream()
//                .map(pair -> Pair.of(pair.getRow() + rowOffset, pair.getColumn() + columnOffset))
//                .collect(Collectors.toList());
//        List<Pair> rotatedCoordinates;
//
//
//        if (Math.abs(rotation) > 0.001) {
//            rotatedCoordinates = RotateSegment.rotateSegment(translatedCoordinates,
//                    pivotRow,
//                    pivotColumn,
//                    // TODO Set proper constants
//                    100,
//                    100,
//                    SQUARE_SIDE,
//                    rotation);
//        } else {
//            rotatedCoordinates = translatedCoordinates;
//        }
//
//        return new SegmentImpl(rotatedCoordinates);

        return null;
    }

    @Override
    public String toString() {
        return "RotationOperation{" +
                "rotation=" + rotation +
                '}';
    }


}
