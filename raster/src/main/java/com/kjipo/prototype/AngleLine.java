package com.kjipo.prototype;

import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import com.kjipo.raster.segment.SegmentImpl;
import com.kjipo.segmentation.LineSegmentationKt;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AngleLine implements AdjustablePrototype {
    private final int id;
    private Pair startPair;
    private double length;
    private double angle;
    private double angleOffset;
    private final Collection<Integer> connectedTo = new HashSet<>();


    private AngleLine(int id, Pair startPair, double length, double angle, Collection<Integer> connectedTo) {
        this.id = id;
        this.startPair = Pair.of(startPair.getRow(), startPair.getColumn());
        this.length = length;
        this.angle = angle;
        this.connectedTo.addAll(connectedTo);
    }

    public AngleLine(int id, Pair startPair, double length, double angle) {
        this(id, startPair, length, angle, Collections.emptySet());
    }

    private AngleLine(int id, Pair startPair, Pair endPair) {
        Objects.requireNonNull(startPair);
        this.id = id;
        int xDelta = endPair.getColumn() - startPair.getColumn();
        int yDelta = endPair.getRow() - startPair.getRow();

        this.startPair = Pair.of(startPair.getRow(), startPair.getColumn());
        this.angle = Math.atan2(yDelta, xDelta);
        this.length = Math.sqrt(Math.pow(xDelta, 2) + Math.pow(yDelta, 2));
    }

    public AngleLine(AngleLine angleLine) {
        this.id = angleLine.id;
        this.startPair = Pair.of(angleLine.startPair.getRow(), angleLine.startPair.getColumn());
        this.length = angleLine.length;
        this.angle = angleLine.angle;
        this.angleOffset = angleLine.angleOffset;
        this.connectedTo.addAll(angleLine.connectedTo);
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

    public int getId() {
        return id;
    }


    public Pair getEndPair() {
        double xDelta = length * Math.cos(angle + angleOffset);
        double yDelta = length * Math.sin(angle + angleOffset);

        return Pair.of((int) Math.round(startPair.getRow() + yDelta), (int) Math.round(startPair.getColumn() + xDelta));
    }

    public Pair getStartPair() {
        return Pair.of(startPair.getRow(), startPair.getColumn());
    }

    public void setStartPair(Pair startPair) {
        Objects.requireNonNull(startPair);
        this.startPair = Pair.of(startPair.getRow(), startPair.getColumn());
    }

    public double getAngle() {
        return angle;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getAngleOffset() {
        return angleOffset;
    }

    public void setAngleOffset(double angleOffset) {
        this.angleOffset = angleOffset;
    }

    public void addAngleOffset(double angleOffset) {
        this.angleOffset += angleOffset;
    }

    public Collection<Integer> getConnectedTo() {
        return new HashSet<>(connectedTo);
    }

    public void addConnectedTo(int id) {
        connectedTo.add(id);
    }

    private AngleLine movePair(FlowDirection flowDirection, Pair startPair, Pair endPair, boolean moveStartPair) {
        if (moveStartPair) {
            return new AngleLine(
                    id,
                    Pair.of(startPair.getRow() + flowDirection.getRowShift(),
                            startPair.getColumn() + flowDirection.getColumnShift()),
                    endPair);
        }
        return new AngleLine(id, startPair,
                Pair.of(endPair.getRow() + flowDirection.getRowShift(),
                        endPair.getColumn() + flowDirection.getColumnShift()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AngleLine angleLine = (AngleLine) o;
        return id == angleLine.id &&
                Double.compare(angleLine.length, length) == 0 &&
                Double.compare(angleLine.angle, angle) == 0 &&
                Double.compare(angleLine.angleOffset, angleOffset) == 0 &&
                Objects.equals(startPair, angleLine.startPair) &&
                Objects.equals(connectedTo, angleLine.connectedTo);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, startPair, length, angle, angleOffset, connectedTo);
    }

    @Override
    public String toString() {
        return "AngleLine{" +
                "id=" + id +
                ", startPair=" + startPair +
                ", length=" + length +
                ", angle=" + angle +
                ", angleOffset=" + angleOffset +
                ", connectedTo=" + connectedTo +
                ", endPair=" +getEndPair() +
                '}';
    }
}
