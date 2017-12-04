package com.kjipo.prototype;

import com.google.common.collect.ImmutableList;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.attraction.LineMoveOperation;
import com.kjipo.raster.attraction.MoveOperation;
import com.kjipo.raster.attraction.PrototypeImpl;
import com.kjipo.raster.attraction.ScaleOperation;
import com.kjipo.raster.match.MatchDistance;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FitPrototype {

    private static final int MAX_ITERATIONS = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(FitPrototype.class);


    public List<Collection<Prototype>> fit(boolean inputData[][]) {
        int numberOfRows = inputData.length;
        int numberOfColumns = inputData[0].length;

        int[][] distanceMap = MatchDistance.computeDistanceMap(inputData);

        List<Collection<Prototype>> prototypeDevelopment = new ArrayList<>();
        prototypeDevelopment.add(Collections.emptyList());

//        Random random = new Random();

        int[][] disjunctRegions = findDisjointRegions(inputData);

        for (int j = 1; j < 10; ++j) {
            LOG.info("Adding prototype: {}", j);
            int addPoint = prototypeDevelopment.size() - 1;

            boolean[][] occupiedData = computeOccupied(prototypeDevelopment.get(addPoint), numberOfRows, numberOfColumns);
            LinePrototype linePrototype = nextStartInRegion(inputData, occupiedData, disjunctRegions, j);

            if (linePrototype == null) {
                LOG.info("No available start point");
                break;
            }

            List<LinePrototype> linePrototypes = Collections.singletonList(linePrototype);
            int scoreUnchanged = 0;


            int bestScore = computeScore2(linePrototype.getSegments().get(0).getPairs(), distanceMap, occupiedData);

            for (int i = 0; i < MAX_ITERATIONS; ++i) {
                // A line prototype only has one segment
                List<kotlin.Pair<LinePrototype, Integer>> newPrototypes = linePrototypes.stream()
                        .map(linePrototype1 -> {
                            Segment segment1 = linePrototype1.getSegments().get(0);
                            Pair startPair = segment1.getPairs().get(0);
                            Pair endPair = segment1.getPairs().get(segment1.getPairs().size() - 1);

                            // If the end points are valid, then all the points in between has to be valid
                            if (!validCoordinates(startPair, numberOfRows, numberOfColumns)
                                    || !validCoordinates(endPair, numberOfRows, numberOfColumns)) {
                                return null;
                            }

                            return new kotlin.Pair<>(new LinePrototype(startPair, endPair),
                                    computeScore2(segment1.getPairs(),
                                            distanceMap,
                                            computeOccupied(prototypeDevelopment.get(addPoint),
                                                    inputData.length,
                                                    inputData[0].length)));
                        })
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(linePrototypeIntegerPair -> -linePrototypeIntegerPair.getSecond()))
                        .limit(3)
                        .collect(Collectors.toList());

                if (newPrototypes.get(0).getSecond() < bestScore) {
                    break;
                } else if (newPrototypes.get(0).getSecond() == bestScore) {
                    if (scoreUnchanged == 5) {
                        break;
                    }
                    ++scoreUnchanged;
                } else {
                    scoreUnchanged = 0;
                }

                LOG.info("Scores:");
                newPrototypes.forEach(linePrototypeIntegerPair -> {
                    LOG.info("Score: {}", linePrototypeIntegerPair.getSecond());
                });
                linePrototypes = newPrototypes.stream().map(kotlin.Pair::getFirst).collect(Collectors.toList());
                bestScore = newPrototypes.get(0).getSecond();

//                result.add(linePrototype);

                Collection<Prototype> newDevelopment = new ArrayList<>();
                newDevelopment.addAll(prototypeDevelopment.get(addPoint));
                newDevelopment.add(linePrototype);

                prototypeDevelopment.add(newDevelopment);
            }
        }

        return prototypeDevelopment;
    }


    public List<Prototype> addSinglePrototype(boolean inputData[][]) {
        int numberOfRows = inputData.length;
        int numberOfColumns = inputData[0].length;

        int[][] distanceMap = MatchDistance.computeDistanceMap(inputData);
        boolean[][] occupiedData = new boolean[numberOfRows][numberOfColumns];

        Pair startPair = nextStartPair(inputData, occupiedData);
        kotlin.Pair<LinePrototype, Integer> originalTop = new kotlin.Pair<>(new LinePrototype(Pair.of(0, 0), Pair.of(0, 3)), 0);

        LinePrototype top = new LinePrototype(startPair, Pair.of(startPair.getRow(), startPair.getColumn() + 3));

        computeOccupied(Collections.singleton(top), numberOfRows, numberOfColumns);

        List<kotlin.Pair<LinePrototype, Integer>> linePrototypeIntegerPair = fitSingleLinePrototype(top, distanceMap, occupiedData, numberOfRows, numberOfColumns);


        LinePrototype right = new LinePrototype(Pair.of(0, 3), Pair.of(3, 3));
        LinePrototype bottom = new LinePrototype(Pair.of(3, 3), Pair.of(3, 0));
        LinePrototype left = new LinePrototype(Pair.of(3, 0), Pair.of(0, 0));


        List<List<LineMoveOperation>> moveOperations = new ArrayList<>();
        LinePrototype previousPrototype = originalTop.getFirst();
        for (kotlin.Pair<LinePrototype, Integer> pair : linePrototypeIntegerPair) {
            moveOperations.add(computeMovements(previousPrototype, pair.getFirst()));
            previousPrototype = pair.getFirst();
        }

//        List<List<LineMoveOperation>> moveOperations = Stream.concat(Stream.of(new kotlin.Pair<>(top, 0)),
//                linePrototypeIntegerPair.stream())
//                .map(pair -> computeMovements(originalTop.getFirst(), pair.getFirst()))
//                .collect(Collectors.toList());

        List<Prototype> collect = new ArrayList<>();

        List<Segment> prototypeSegments = new ArrayList<>();

//                    prototypeSegments.addAll(top.getSegments());
        prototypeSegments.addAll(right.getSegments());
//                    prototypeSegments.addAll(bottom.getSegments());
//                    prototypeSegments.addAll(left.getSegments());

        Prototype prototype = new PrototypeImpl(prototypeSegments);

        Prototype prototypeToApplyMovementsTo = prototype;

        for (List<LineMoveOperation> moveOperation : moveOperations) {
            List<Prototype> prototypes = applyMoveOperations(prototypeToApplyMovementsTo, moveOperation);
            // Only interested in the last prototype in the sequence here
            collect.add(prototypes.get(prototypes.size() - 1));

            prototypeToApplyMovementsTo = prototypes.get(prototypes.size() - 1);
        }

        return collect;
    }

    public static List<Prototype> applyMoveOperations(Prototype prototype, Collection<LineMoveOperation> moveOperations) {
        List<Prototype> moves = new ArrayList<>();
        moves.add(prototype);

        for (LineMoveOperation lineMoveOperation : moveOperations) {
            prototype = new PrototypeImpl(moves.get(moves.size() - 1).getSegments().stream()
                    .map(lineMoveOperation::applyToLine)
                    .collect(Collectors.toList()));
            moves.add(prototype);
        }

        return moves;
    }


    private static List<LineMoveOperation> computeMovements(LinePrototype originalPrototype, LinePrototype processedPrototype) {
        Pair originalStartPair = originalPrototype.getStartPair();
        Pair processedStartPair = processedPrototype.getStartPair();
        List<LineMoveOperation> moveOperations = new ArrayList<>();

        int rowShift = processedStartPair.getRow() - originalStartPair.getRow();
        int columnShift = processedStartPair.getColumn() - originalStartPair.getColumn();

        MoveOperation moveOperation = new MoveOperation(rowShift,
                columnShift,
                0,
                // The pivot point is not used in this operation
                originalStartPair.getRow(),
                originalStartPair.getColumn());

        moveOperations.add(moveOperation);

        int deltaDistance = Math.round((float) (processedPrototype.getDistance() - originalPrototype.getDistance()));
        moveOperations.add(new ScaleOperation(deltaDistance));

        Pair originalEndPair = originalPrototype.getEndPair();
        Pair processedEndPair = processedPrototype.getEndPair();
        double rotationAngle = Math.atan2((double) processedEndPair.getRow() - originalEndPair.getRow() - rowShift,
                (double) processedEndPair.getColumn() - originalEndPair.getColumn() - columnShift);

        LOG.info("Rotation angle: {}", rotationAngle);

        MoveOperation rotation = new MoveOperation(0, 0, rotationAngle,
                processedStartPair.getRow() + rowShift,
                processedStartPair.getColumn() + columnShift);
        moveOperations.add(rotation);

        return ImmutableList.copyOf(moveOperations);
    }


    private static List<kotlin.Pair<LinePrototype, Integer>> fitSingleLinePrototype(LinePrototype linePrototype, int distanceMap[][],
                                                                                    boolean occupiedData[][],
                                                                                    int numberOfRows, int numberOfColumns) {
        int scoreUnchanged = 0;
        int bestScore = computeScore2(linePrototype.getSegments().get(0).getPairs(), distanceMap, occupiedData);
        int previousBestScore = bestScore;

        PriorityQueue<kotlin.Pair<LinePrototype, Integer>> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(pair -> -pair.getSecond()));

        kotlin.Pair<LinePrototype, Integer> firstPair = new kotlin.Pair<>(linePrototype, bestScore);
        List<kotlin.Pair<LinePrototype, Integer>> bestScorePairs = new ArrayList<>();

        priorityQueue.add(firstPair);
        bestScorePairs.add(firstPair);

        for (int i = 0; i < MAX_ITERATIONS; ++i) {
            // A line prototype only has one segment

            // TODO Mostly for testing to see if the search is able to find a good fit
            kotlin.Pair<LinePrototype, Integer> nextPrototype = priorityQueue.poll();

            LOG.info("Checking prototype: {}", nextPrototype);

            if (nextPrototype.getSecond() > bestScore) {

                LOG.info("New best score: {}", nextPrototype);

                previousBestScore = bestScore;
                bestScore = nextPrototype.getSecond();
                bestScorePairs.add(nextPrototype);
            }

            nextPrototype.component1().getMovements()
                    .map(linePrototype1 -> {
                        Segment segment1 = linePrototype1.getSegments().get(0);
                        Pair startPair = segment1.getPairs().get(0);
                        Pair endPair = segment1.getPairs().get(segment1.getPairs().size() - 1);

                        // If the end points are valid, then all the points in between has to be valid
                        if (!validCoordinates(startPair, numberOfRows, numberOfColumns)
                                || !validCoordinates(endPair, numberOfRows, numberOfColumns)) {
                            return null;
                        }

                        return new kotlin.Pair<>(new LinePrototype(startPair, endPair),
                                computeScore2(segment1.getPairs(),
                                        distanceMap,
                                        occupiedData));
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(linePrototypeIntegerPair -> -linePrototypeIntegerPair.getSecond()))
                    .limit(5)
                    .forEach(priorityQueue::add);

            if (previousBestScore == bestScore) {
                if (scoreUnchanged == 50) {
                    break;
                }
                ++scoreUnchanged;
            } else {
                scoreUnchanged = 0;
            }

//            LOG.info("Scores:");
//            newPrototypes.forEach(linePrototypeIntegerPair -> {
//                LOG.info("Score: {}", linePrototypeIntegerPair.getSecond());
//            });
//            linePrototypes = newPrototypes.stream().map(kotlin.Pair::getFirst).collect(Collectors.toList());
//            bestScore = newPrototypes.get(0).getSecond();

        }

        LOG.info("Best score: {}", bestScore);

        return bestScorePairs;
    }

    private static int computeScore(List<Pair> pairs, int distanceMatrix[][], boolean occupiedMatrix[][]) {
        return pairs.stream()
                .mapToInt(pair -> {
                    int score = 0;
                    int distance = distanceMatrix[pair.getRow()][pair.getColumn()];
                    if (distance > 0) {
                        score += -distance;
                    } else {
                        score += 1;
                    }
                    if (occupiedMatrix[pair.getRow()][pair.getColumn()]) {
                        score += -50;
                    }
                    return score;
                }).sum();

    }

    private static int computeScore2(List<Pair> pairs, int distanceMatrix[][], boolean occupiedMatrix[][]) {
        Pair startPair = pairs.get(0);
        Pair stopPair = pairs.get(pairs.size() - 1);

        int lengthScore = 0;
        if (distanceMatrix[startPair.getRow()][startPair.getColumn()] == 0
                && distanceMatrix[stopPair.getRow()][stopPair.getColumn()] == 0) {

            lengthScore = pairs.size();

            LOG.debug("Length score: " + lengthScore);
        }

        return Stream.of(startPair, stopPair)
                .mapToInt(pair -> {
                    int score = 0;
                    int distance = distanceMatrix[pair.getRow()][pair.getColumn()];

                    LOG.debug("Distance: " + distance);

                    if (distance > 0) {
                        score += -distance;
                    } else {
                        score += 1;
                    }
                    if (occupiedMatrix[pair.getRow()][pair.getColumn()]) {
                        score += -50;
                    }
                    return score;
                }).sum()
                + lengthScore;

    }

    private static boolean validCoordinates(Pair pair, int numberOfRows, int numberOfColumns) {
        return !(pair.getRow() < 0
                || pair.getRow() >= numberOfRows
                || pair.getColumn() < 0
                || pair.getColumn() >= numberOfColumns);
    }


    private static boolean[][] computeOccupied(Collection<Prototype> prototypes, int rows, int columns) {
        boolean result[][] = new boolean[rows][columns];
        prototypes.stream()
                .flatMap(prototype -> prototype.getSegments().stream())
                .flatMap(segment -> segment.getPairs().stream())
                .forEach(pair -> result[pair.getRow()][pair.getColumn()] = false);

        return result;
    }

    private static LinePrototype nextStart(boolean inputData[][], boolean occupiedData[][]) {
        for (int row = 0; row < inputData.length; ++row) {
            for (int column = 0; column < inputData[0].length; ++column) {
                if (inputData[row][column]
                        && !occupiedData[row][column]) {
                    return new LinePrototype(Pair.of(row, column), Pair.of(row, column));
                }
            }
        }
        return null;
    }

    private static Pair nextStartPair(boolean inputData[][], boolean occupiedData[][]) {
        for (int row = 0; row < inputData.length; ++row) {
            for (int column = 0; column < inputData[0].length; ++column) {
                if (inputData[row][column]
                        && !occupiedData[row][column]) {
                    return Pair.of(row, column);
                }
            }
        }
        return null;
    }

    private static LinePrototype nextStartInRegion(boolean inputData[][], boolean occupiedData[][],
                                                   int regionData[][], int regionValue) {
        for (int row = 0; row < inputData.length; ++row) {
            for (int column = 0; column < inputData[0].length; ++column) {
                if (inputData[row][column]
                        && !occupiedData[row][column]
                        && regionData[row][column] == regionValue) {
                    return new LinePrototype(Pair.of(row, column), Pair.of(row, column));
                }
            }
        }
        return null;
    }

    private static int[][] findDisjointRegions(boolean inputData[][]) {
        int regionData[][] = new int[inputData.length][inputData[0].length];
        int fillValue = 1;
        boolean foundHit = true;

        while (foundHit) {
            foundHit = false;
            for (int row = 0; row < inputData.length; ++row) {
                for (int column = 0; column < inputData[0].length; ++column) {
                    if (inputData[row][column]
                            && regionData[row][column] == 0) {
                        spreadAcrossRegion(row, column, fillValue++, inputData, regionData);
                        foundHit = true;
                    }
                    if (foundHit) {
                        break;
                    }
                }
                if (foundHit) {
                    break;
                }

            }

        }
        return regionData;
    }


    private static void spreadAcrossRegion(int startRow, int startColumn, int fillValue,
                                           boolean inputData[][], int regionData[][]) {
        Deque<Pair> cellsToVisit = new ArrayDeque<>();
        cellsToVisit.add(Pair.of(startRow, startColumn));

        while (!cellsToVisit.isEmpty()) {
            Pair pair = cellsToVisit.poll();

            int row = pair.getRow();
            int column = pair.getColumn();

            if (EncodingUtilities.validCoordinates(row, column, inputData.length, inputData[0].length)
                    && inputData[row][column]) {
                regionData[row][column] = fillValue;
            }

            for (FlowDirection flowDirection : FlowDirection.values()) {
                int nextRow = row + flowDirection.getRowShift();
                int nextColumn = column + flowDirection.getColumnShift();
                Pair nextPair = Pair.of(nextRow, nextColumn);

                if (EncodingUtilities.validCoordinates(nextRow, nextColumn, inputData.length, inputData[0].length)
                        && inputData[nextRow][nextColumn]
                        && regionData[nextRow][nextColumn] == 0
                        && !cellsToVisit.contains(nextPair)) {
                    cellsToVisit.add(nextPair);
                }
            }
        }
    }


}
