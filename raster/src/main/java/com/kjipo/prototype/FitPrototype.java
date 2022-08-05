package com.kjipo.prototype;

import com.google.common.collect.Lists;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.attraction.AngleLineMoveOperation;
import com.kjipo.raster.attraction.AngleLineMoveOperationImpl;
import com.kjipo.raster.attraction.LineMoveOperation;
import com.kjipo.raster.attraction.PrototypeImpl;
import com.kjipo.raster.match.MatchDistance;
import com.kjipo.representation.prototype.AdjustablePrototype;
import com.kjipo.representation.prototype.AngleLine;
import com.kjipo.representation.prototype.LinePrototype;
import com.kjipo.representation.prototype.Prototype;
import com.kjipo.representation.raster.FlowDirection;
import com.kjipo.representation.segment.Pair;
import com.kjipo.representation.segment.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class FitPrototype {

    private static final int MAX_ITERATIONS = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(FitPrototype.class);


    public List<Prototype> addPrototypes(boolean inputData[][], Collection<AngleLine> prototype, boolean includeHistory) {
        List<Prototype> result = new ArrayList<>();

        if (includeHistory) {
            result.addAll(addSinglePrototype2(inputData, prototype, 0, 0));
        } else {
            if (result.isEmpty()) {
                List<Prototype> stepsInAddingPrototype = addSinglePrototype2(inputData, prototype, 0, 0);
                result.add(stepsInAddingPrototype.get(stepsInAddingPrototype.size() - 1));
            } else {
                result.addAll(addSinglePrototype2(inputData, prototype, 0, 0));
            }
        }

        return result;
    }


    public List<Prototype> addSinglePrototype2(boolean inputData[][], Collection<AngleLine> prototype, int initialRowOffset, int initialColumnOffset) {
        int numberOfRows = inputData.length;
        int numberOfColumns = inputData[0].length;
        int[][] distanceMap = MatchDistance.computeDistanceMap(inputData);
        boolean[][] occupiedData = new boolean[numberOfRows][numberOfColumns];
        AngleLine originalFirst = new AngleLine(prototype.iterator().next());

        AngleLine shiftedFirst = new AngleLine(originalFirst);
        shiftedFirst.setStartPair(new Pair(originalFirst.getStartPair().getRow() + initialRowOffset,
                originalFirst.getStartPair().getColumn() + initialColumnOffset));

        // Fit line segment in prototype
        List<kotlin.Pair<AngleLine, Integer>> fittedPrototypes = fitSingleLinePrototype(shiftedFirst, distanceMap, numberOfRows, numberOfColumns);


        kotlin.Pair<AngleLine, Integer> angleLineIntegerPair = fittedPrototypes.get(fittedPrototypes.size() - 1);


        LOG.info("Best fit: " + angleLineIntegerPair);


// TODO Use defined constant
        if (angleLineIntegerPair.getSecond() < -500) {
            return Collections.emptyList();
        }

        // TODO Code commented out to make it easier to see what happens when adding single line prototype

        return Lists.newArrayList(angleLineIntegerPair.component1());


        /*

        List<kotlin.Pair<AngleLine, Integer>> startEndPrototype = Lists.newArrayList(new kotlin.Pair<>(shiftedFirst, 0), fittedPrototypes.get(fittedPrototypes.size() - 1));

        // Move the line into position
        List<List<AngleLineMoveOperation>> moveOperations = new ArrayList<>();
        AngleLine previousPrototype = new AngleLine(originalFirst);
        for (kotlin.Pair<AngleLine, Integer> pair : startEndPrototype) {
            moveOperations.add(computeMovements(previousPrototype, pair.getFirst()));
            previousPrototype = pair.getFirst();
        }

        // Add the lines in the prototype to a list in the
        // order they need to be processed by looking at
        // which lines are connected
        List<Integer> processedLines = new ArrayList<>();
        while (processedLines.size() < prototype.size()) {
            for (AngleLine angleLine : prototype) {
                if (!processedLines.contains(angleLine.getId())
                        && processedLines.containsAll(angleLine.getConnectedTo())) {
                    processedLines.add(angleLine.getId());
                }
            }
        }

        // Create a mapping between the ID of the lines and the lines in the prototype
        Map<Integer, AngleLine> idLineMap = prototype.stream()
                .collect(Collectors.toMap(AngleLine::getId, AngleLine::new));

        List<AngleLine> iterationOrder = processedLines.stream()
                .map(idLineMap::get)
                .collect(Collectors.toList());
        Collections.reverse(iterationOrder);

        iterationOrder.forEach(angleLine -> angleLine.getConnectedTo().stream()
                .map(idLineMap::get)
                .forEach(connectedTo -> {
                    connectedTo.setStartPair(angleLine.getEndPair());
                    connectedTo.addAngleOffset(angleLine.getAngle() + angleLine.getAngleOffset());
                }));


        // Make a copy of the prototype
        List<Prototype> collect = new ArrayList<>();
        List<AngleLine> originalConfiguration = iterationOrder.stream().map(AngleLine::new).collect(Collectors.toList());
        collect.add(new PrototypeCollection<>(originalConfiguration));

        // Apply move operations to all segments in prototype
        for (List<AngleLineMoveOperation> moveOperation : moveOperations) {
            for (AngleLineMoveOperation lineMoveOperation : moveOperation) {
                boolean first = true;
                for (AngleLine angleLine : iterationOrder) {
                    if (first) {
                        lineMoveOperation.apply(angleLine);
                        first = false;
                    } else {
                        lineMoveOperation.applyStretching(angleLine);
                    }
                    angleLine.getConnectedTo().stream()
                            .map(idLineMap::get)
                            .forEach(angleLine1 -> {
                                angleLine1.setStartPair(angleLine.getEndPair());
                                angleLine1.addAngleOffset(lineMoveOperation.getRotation());
                            });
                }
            }

            List<AngleLine> linesToAdd = iterationOrder.stream().map(AngleLine::new).collect(Collectors.toList());

            // TODO Only here for debugging
            for (AngleLine angleLine : linesToAdd) {
                List<Segment> segments = angleLine.getSegments();
                for (Segment segment : segments) {
                    for (Pair pair : segment.getPairs()) {
                        if(pair.getRow() < 0 || pair.getRow() >= inputData.length || pair.getColumn() < 0 || pair.getColumn() > inputData[0].length) {
                            System.out.println("Test30");
                        }

                    }

                }


            }

            collect.add(new PrototypeCollection<>(linesToAdd));
        }

        return collect;

        */
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


    private static List<AngleLineMoveOperation> computeMovements(AngleLine originalPrototype,
                                                                 AngleLine processedPrototype) {
        // TODO An assumption is made here that each prototype only has one segment
        Pair originalStartPair = originalPrototype.getSegments().get(0).getPairs().get(0);
        Pair processedStartPair = processedPrototype.getSegments().get(0).getPairs().get(0);


        int rowShift = processedStartPair.getRow() - originalStartPair.getRow();
        int columnShift = processedStartPair.getColumn() - originalStartPair.getColumn();

        double deltaLength = processedPrototype.getLength() - originalPrototype.getLength();
        double rotationAngle = processedPrototype.getAngle() - originalPrototype.getAngle();

//        LOG.info("Rotation angle: {}", rotationAngle);

        return Collections.singletonList(new AngleLineMoveOperationImpl(rowShift, columnShift, deltaLength, rotationAngle));
    }


    private static <T extends AdjustablePrototype> List<kotlin.Pair<T, Integer>> fitSingleLinePrototype(T linePrototype, int distanceMap[][],
                                                                                                        int numberOfRows, int numberOfColumns) {
        int scoreUnchanged = 0;
        int bestScore = computeScore3(linePrototype.getSegments().get(0).getPairs(), distanceMap);
        int previousBestScore = bestScore;

        PriorityQueue<kotlin.Pair<T, Integer>> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(pair -> -pair.getSecond()));

        kotlin.Pair<T, Integer> firstPair = new kotlin.Pair<>(linePrototype, bestScore);
        List<kotlin.Pair<T, Integer>> bestScorePairs = new ArrayList<>();

        priorityQueue.add(firstPair);
        bestScorePairs.add(firstPair);

        Set<T> tabooSearchSet = new HashSet<>();

        for (int i = 0; i < MAX_ITERATIONS; ++i) {
            // A line prototype only has one segment
            if (priorityQueue.isEmpty()) {
                break;
            }

            // TODO Mostly for testing to see if the search is able to find a good fit
            kotlin.Pair<T, Integer> nextPrototype = priorityQueue.poll();


            if (tabooSearchSet.contains(nextPrototype.getFirst())) {
                continue;
            } else {
                tabooSearchSet.add(nextPrototype.getFirst());
            }


//            LOG.info("Checking prototype: {}", nextPrototype);

            if (nextPrototype.getSecond() > bestScore) {

//                LOG.info("New best score: {}", nextPrototype);

                previousBestScore = bestScore;
                bestScore = nextPrototype.getSecond();
                bestScorePairs.add(nextPrototype);
            }

            nextPrototype.component1().getMovements().stream().map(linePrototype1 -> {
                        Segment segment1 = linePrototype1.getSegments().get(0);

                        for (Pair pair : segment1.getPairs()) {
                            if (!validCoordinates(pair, numberOfRows, numberOfColumns)) {
                                return null;
                            }
                        }

//                        Pair startPair = segment1.getPairs().get(0);
//                        Pair endPair = segment1.getPairs().get(segment1.getPairs().size() - 1);
//                        // If the end points are valid, then all the points in between have to be valid
//                        if (!validCoordinates(startPair, numberOfRows, numberOfColumns)
//                                || !validCoordinates(endPair, numberOfRows, numberOfColumns)) {
//                            return null;
//                        }


                        int score = computeScore3(segment1.getPairs(),
                                distanceMap);

//                        LOG.info("Score: " +score +". Checking prototype: " +linePrototype.getSegments().stream().flatMap(segment -> segment.getPairs().stream()).collect(Collectors.toList()));

                        return new kotlin.Pair<>((T) linePrototype1, score);
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(linePrototypeIntegerPair -> -linePrototypeIntegerPair.getSecond()))
//                    .limit(5)
                    .forEach(priorityQueue::add);

            if (previousBestScore == bestScore) {
                if (scoreUnchanged == 50) {
                    break;
                }
                ++scoreUnchanged;
            } else {
                scoreUnchanged = 0;
            }
        }

//        LOG.info("Best score: {}", bestScore);

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

        if (startPair.getRow() < 0 || startPair.getColumn() >= distanceMatrix[0].length
                || stopPair.getRow() < 0 || stopPair.getColumn() >= distanceMatrix[0].length) {
            return Integer.MIN_VALUE;
        }

        int lengthScore = 0;
        if (distanceMatrix[startPair.getRow()][startPair.getColumn()] == 0
                && distanceMatrix[stopPair.getRow()][stopPair.getColumn()] == 0) {

            lengthScore = pairs.size();

            LOG.debug("Length score: " + lengthScore);
        }

        return pairs.stream()
                .mapToInt(pair -> {
                    int score = 0;

                    if (pair.getRow() >= distanceMatrix.length || pair.getColumn() >= distanceMatrix[0].length) {
                        LOG.warn("Point is outside matrix. Distance matrix dimensions: " + distanceMatrix.length + ", " + distanceMatrix[0].length + ". Pair: " + pair);
                        return Integer.MIN_VALUE;
                    }

                    int distance = distanceMatrix[pair.getRow()][pair.getColumn()];

                    LOG.debug("Distance: " + distance);

                    if (distance > 0) {
                        score += -distance;
                    } else {
                        score += 1;
                    }
//                    if (occupiedMatrix[pair.getRow()][pair.getColumn()]) {
//                        score += -50;
//                    }
                    return score;
                }).sum();
//                + lengthScore;

    }


    private static int computeScore3(List<Pair> pairs, int distanceMatrix[][]) {
        Pair startPair = pairs.get(0);
        Pair stopPair = pairs.get(pairs.size() - 1);

        if (startPair.getRow() < 0 || startPair.getColumn() >= distanceMatrix[0].length
                || stopPair.getRow() < 0 || stopPair.getColumn() >= distanceMatrix[0].length) {
            return -1000;
        }

        int score = 0;
        for (Pair pair : pairs) {
            if (pair.getRow() < 0
                    || pair.getRow() >= distanceMatrix.length
                    || pair.getColumn() < 0
                    || pair.getColumn() >= distanceMatrix[0].length) {
                LOG.warn("Point is outside matrix. Distance matrix dimensions: " + distanceMatrix.length + ", " + distanceMatrix[0].length + ". Pair: " + pair);
                return -1000;
            }

            int distance = distanceMatrix[pair.getRow()][pair.getColumn()];

            LOG.debug("Distance: " + distance);

            if (distance > 0) {
                return -1000;
            } else {
                score += 1;
            }
        }
        return score;
    }

    public static boolean validCoordinates(int row, int column, int numberOfRows, int numberOfColumns) {
        return row >= 0
                && row < numberOfRows
                && column >= 0
                && column < numberOfColumns;
    }

    public static boolean validCoordinates(Pair pair, int numberOfRows, int numberOfColumns) {
        return pair.getRow() >= 0
                && pair.getRow() < numberOfRows
                && pair.getColumn() >= 0
                && pair.getColumn() < numberOfColumns;
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
                    return new LinePrototype(new Pair(row, column), new Pair(row, column));
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
                    return new Pair(row, column);
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
                    return new LinePrototype(new Pair(row, column), new Pair(row, column));
                }
            }
        }
        return null;
    }

    public static int[][] findDisjointRegions(boolean inputData[][]) {
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

    public static int findNumberOfDisjointRegions(boolean inputData[][]) {
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
        return fillValue;
    }


    private static void spreadAcrossRegion(int startRow, int startColumn, int fillValue,
                                           boolean inputData[][], int regionData[][]) {
        Deque<Pair> cellsToVisit = new ArrayDeque<>();
        cellsToVisit.add(new Pair(startRow, startColumn));

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
                Pair nextPair = new Pair(nextRow, nextColumn);

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
