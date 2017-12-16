package com.kjipo.prototype;

import com.kjipo.raster.EncodingUtilities;
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

public class JointLine implements AdjustablePrototype {
    private final Joint startJoint;
    private final Joint endJoint;
    private final boolean shouldScale;


    public JointLine(Joint startJoint, Joint endJoint, boolean shouldScale) {
        this.startJoint = startJoint;
        this.endJoint = endJoint;
        this.shouldScale = shouldScale;
    }

    public JointLine(Pair startPair, Pair endPair, boolean shouldScale) {
        startJoint = new Joint(startPair);
        endJoint = new Joint(endPair);
        this.shouldScale = shouldScale;
    }

    @Override
    public List<Segment> getSegments() {
        kotlin.Pair newStartPair = new kotlin.Pair<>(startJoint.getJoint().getRow(), startJoint.getJoint().getColumn());
        kotlin.Pair newEndPair = new kotlin.Pair<>(endJoint.getJoint().getRow(), endJoint.getJoint().getColumn());
        List<kotlin.Pair<Integer, Integer>> linePairs = LineSegmentationKt.computeLine(newStartPair, newEndPair);

        return Collections.singletonList(new SegmentImpl(linePairs.stream()
                .map(kotlinPair -> new Pair(kotlinPair.component1(), kotlinPair.component2()))
                .collect(Collectors.toList())));
    }

    @Override
    public Stream<? extends AdjustablePrototype> getMovements() {
        return Arrays.stream(FlowDirection.values())
                .flatMap(flowDirection ->
                        Stream.of(moveStartPair(flowDirection), moveEndPair(flowDirection)))
                .filter(jointLine -> jointLine.startJoint.getJoint().getRow() >= 0 && jointLine.startJoint.getJoint().getColumn() >= 0)
                .filter(jointLine -> jointLine.endJoint.getJoint().getRow() >= 0 && jointLine.endJoint.getJoint().getColumn() >= 0)
                .filter(linePrototype -> !linePrototype.startJoint.getJoint().equals(linePrototype.endJoint.getJoint()));
    }

    private JointLine moveStartPair(FlowDirection flowDirection) {
        return new JointLine(new Joint(new Pair(startJoint.getJoint().getRow() + flowDirection.getRowShift(),
                startJoint.getJoint().getColumn() + flowDirection.getColumnShift())),
                new Joint(new Pair(endJoint.getJoint().getRow(), endJoint.getJoint().getColumn())), shouldScale);
    }

    private JointLine moveEndPair(FlowDirection flowDirection) {
        return new JointLine(new Joint(new Pair(startJoint.getJoint().getRow(),
                startJoint.getJoint().getColumn())),
                new Joint(new Pair(endJoint.getJoint().getRow() + flowDirection.getRowShift(),
                        endJoint.getJoint().getColumn() + flowDirection.getColumnShift())), shouldScale);
    }

    public void stretch(int scaling) {
        if(!shouldScale) {
            return;
        }

        List<Pair> pairs = computeLine(startJoint, endJoint);

        // TODO This might not work properly if scaling is large
        if (scaling > 0) {
            Pair lastPair = pairs.get(pairs.size() - 1);
            Pair nextToLastPair = pairs.get(pairs.size() - 2);

            FlowDirection flowDirection = EncodingUtilities.computeDirection(nextToLastPair, lastPair);

            // A line prototype has only one segment
            endJoint.setJoint(Pair.of(lastPair.getRow() + scaling * flowDirection.getRowShift(),
                    lastPair.getColumn() + scaling * flowDirection.getColumnShift()));

        } else if (scaling < 0) {
            if (Math.abs(scaling) < pairs.size() - 2) {
                endJoint.setJoint(pairs.get(pairs.size() + scaling));
            }
        }
    }

//    public void translate() {
//        List<Pair> translatedCoordinates = linePrototype.getPairs().stream()
//                .map(pair -> Pair.of(pair.getRow() + rowOffset, pair.getColumn() + columnOffset))
//                .collect(Collectors.toList())
//
//    }

    private static List<Pair> computeLine(Joint startJoint, Joint endJoint) {
        return LineSegmentationKt.computeLine(new kotlin.Pair<>(startJoint.getJoint().getRow(), startJoint.getJoint().getColumn()),
                new kotlin.Pair<>(endJoint.getJoint().getRow(), endJoint.getJoint().getColumn())).stream()
                .map(pair1 -> new Pair(pair1.component1(), pair1.getSecond()))
                .collect(Collectors.toList());

    }

    public Pair getStartPair() {
        return startJoint.getJoint();
    }

    public Pair getEndPair() {
        return endJoint.getJoint();
    }

    public boolean isShouldScale() {
        return shouldScale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JointLine jointLine = (JointLine) o;
        return shouldScale == jointLine.shouldScale &&
                Objects.equals(startJoint, jointLine.startJoint) &&
                Objects.equals(endJoint, jointLine.endJoint);
    }

    @Override
    public int hashCode() {

        return Objects.hash(startJoint, endJoint, shouldScale);
    }

    @Override
    public String toString() {
        return "JointLine{" +
                "startJoint=" + startJoint +
                ", endJoint=" + endJoint +
                ", shouldScale=" + shouldScale +
                '}';
    }
}
