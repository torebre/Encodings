package com.kjipo.prototype;

import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import com.kjipo.segmentation.LineSegmentationKt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AngleLine implements AdjustablePrototype {
    private Pair startPair;
    private double length;
    private double angle;
    private double angleOffset;


    public AngleLine(Pair startPair, double length, double angle) {
        this.startPair = startPair;
        this.length = length;
        this.angle = angle;
    }

    public AngleLine(Pair startPair, Pair endPair) {
        int xDelta = endPair.getColumn() - startPair.getColumn();
        int yDelta = endPair.getRow() - startPair.getRow();

        this.startPair = startPair;
        this.angle = Math.atan2(yDelta, xDelta);
        this.length = Math.sqrt(Math.pow(xDelta, 2) + Math.pow(yDelta, 2));
    }

    public AngleLine(AngleLine angleLine) {
        this.startPair = angleLine.startPair;
        this.length = angleLine.length;
        this.angle = angleLine.angle;
        this.angleOffset = angleLine.angleOffset;
    }

    @Override
    public Stream<? extends AdjustablePrototype> getMovements() {
        Pair endPair = getEndPair();

        return Arrays.stream(FlowDirection.values())
                .flatMap(flowDirection ->
                        Stream.of(movePair(flowDirection, startPair, endPair, true), movePair(flowDirection, startPair, endPair, false)))
                .filter(angleLine -> angleLine.startPair.getRow() >= 0 && angleLine.startPair.getColumn() >= 0)
                .filter(angleLine -> angleLine.getEndPair().getRow() >= 0 && angleLine.getEndPair().getColumn() >= 0)
                .filter(angleLine -> !angleLine.startPair.equals(angleLine.getEndPair()));
    }

    @Override
    public List<Segment> getSegments() {
        kotlin.Pair newStartPair = new kotlin.Pair<>(startPair.getRow(), startPair.getColumn());
        Pair endPair = getEndPair();
        kotlin.Pair newEndPair = new kotlin.Pair<>(endPair.getRow(), endPair.getColumn());

        List<kotlin.Pair<Integer, Integer>> linePairs = LineSegmentationKt.computeLine(newStartPair, newEndPair);

        return Collections.singletonList(new SegmentImpl(linePairs.stream()
                .map(kotlinPair -> new Pair(kotlinPair.component1(), kotlinPair.component2()))
                .collect(Collectors.toList())));
    }


    public void stretch(int scaling) {
        length += scaling;
    }

    public Pair getEndPair() {
        double xDelta = length * Math.cos(angle + angleOffset);
        double yDelta = length * Math.sin(angle + angleOffset);
        return Pair.of((int) Math.round(startPair.getRow() + yDelta),
                (int) Math.round(startPair.getColumn() + xDelta));
    }

    public Pair getStartPair() {
        return startPair;
    }

    public void setStartPair(Pair startPair) {
        this.startPair = startPair;
    }

    public double getAngle() {
        return angle;
    }

    public double getLength() {
        return length;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public void setAngleOffset(double angleOffset) {
        this.angleOffset = angleOffset;
    }

    private AngleLine movePair(FlowDirection flowDirection, Pair startPair, Pair endPair, boolean moveStartPair) {
        if (moveStartPair) {
            return new AngleLine(
                    Pair.of(startPair.getRow() + flowDirection.getRowShift(),
                            startPair.getColumn() + flowDirection.getColumnShift()),
                    endPair);
        }
        return new AngleLine(startPair,
                Pair.of(endPair.getRow() + flowDirection.getRowShift(),
                        endPair.getColumn() + flowDirection.getColumnShift()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AngleLine angleLine = (AngleLine) o;
        return Double.compare(angleLine.length, length) == 0 &&
                Double.compare(angleLine.angle, angle) == 0 &&
                Objects.equals(startPair, angleLine.startPair);
    }

    @Override
    public int hashCode() {

        return Objects.hash(startPair, length, angle);
    }

    @Override
    public String toString() {
        return "AngleLine{" +
                "startPair=" + startPair +
                ", length=" + length +
                ", angle=" + angle +
                "}, End pair: " + getEndPair();
    }
}
