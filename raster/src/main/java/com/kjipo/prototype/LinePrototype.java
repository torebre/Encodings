package com.kjipo.prototype;


import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import com.kjipo.segmentation.LineSegmentationKt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LinePrototype implements Prototype {
    private final Pair startPair;
    private final Pair endPair;


    public LinePrototype(Pair startPair, Pair endPair) {
        this.startPair = startPair;
        this.endPair = endPair;
    }


    @Override
    public List<Segment> getSegments() {
        kotlin.Pair newStartPair = new kotlin.Pair<>(startPair.getRow(), startPair.getColumn());
        kotlin.Pair newEndPair = new kotlin.Pair<>(endPair.getRow(), endPair.getColumn());
        List<kotlin.Pair<Integer, Integer>> linePairs = LineSegmentationKt.computeLine(newStartPair, newEndPair);

        return Collections.singletonList(new SegmentImpl(linePairs.stream()
                .map(kotlinPair -> new Pair(kotlinPair.component1(), kotlinPair.component2()))
                .collect(Collectors.toList())));
    }

    private LinePrototype moveStartPair(FlowDirection flowDirection) {
        return new LinePrototype(new Pair(startPair.getRow() + flowDirection.getRowShift(), startPair.getColumn() + flowDirection.getColumnShift()),
                new Pair(endPair.getRow(), endPair.getColumn()));
    }

    private LinePrototype moveEndPair(FlowDirection flowDirection) {
        return new LinePrototype(new Pair(startPair.getRow(), startPair.getColumn()),
                new Pair(endPair.getRow() + flowDirection.getRowShift(), endPair.getColumn() + flowDirection.getColumnShift()));
    }

    public Stream<LinePrototype> getMovements() {
        return Arrays.stream(FlowDirection.values())
                .flatMap(flowDirection ->
                        Stream.of(moveStartPair(flowDirection), moveEndPair(flowDirection)))
                .filter(linePrototype -> !linePrototype.startPair.equals(linePrototype.endPair));
    }

    public Pair getStartPair() {
        return startPair;
    }

    public Pair getEndPair() {
        return endPair;
    }

    public double getDistance() {
        return Math.sqrt(Math.pow(endPair.getRow() - startPair.getRow(), 2)
                + Math.pow(endPair.getColumn() - startPair.getColumn(), 2));
    }

    @Override
    public String toString() {
        return "LinePrototype{" +
                "startPair=" + startPair +
                ", endPair=" + endPair +
                '}';
    }
}
