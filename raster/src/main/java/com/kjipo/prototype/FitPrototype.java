package com.kjipo.prototype;

import com.google.common.collect.Lists;
import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.attraction.*;
import com.kjipo.raster.match.MatchDistance;
import com.kjipo.raster.segment.Pair;
import com.kjipo.raster.segment.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FitPrototype {

    private static final int MAX_ITERATIONS = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(FitPrototype.class);


    public List<Collection<Prototype>> fit(boolean inputData[][]) {
        int numberOfRows = inputData.length;
        int numberOfColumns = inputData[0].length;
        int[][] distanceMap = MatchDistance.computeDistanceMap(inputData);

        List<Collection<Prototype>> prototypeDevelopment = new ArrayList<>();
        prototypeDevelopment.add(Collections.emptyList());
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

            List<LinePrototype> linePrototypes = new ArrayList<>();
            linePrototypes.add(linePrototype);
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


    public List<Prototype> addPrototypes(boolean inputData[][], Collection<AngleLine> prototype) {
        return addPrototypes(inputData, prototype, true);
    }

    public List<Prototype> addPrototypes(boolean inputData[][], Collection<AngleLine> prototype, boolean includeHistory) {
        int[][] disjunctRegions = findDisjointRegions(inputData);
        int current = 1;
        List<Prototype> result = new ArrayList<>();


        while (true) {
            int startRow = -1;
            int startColumn = -1;

            for (int row = 0; row < disjunctRegions.length; ++row) {
                for (int column = 0; column < disjunctRegions[0].length; ++column) {
                    if (disjunctRegions[row][column] == current) {
                        startRow = row;
                        startColumn = column;
                        break;
                    }
                }
                if (startRow != -1) {
                    break;
                }
            }

            if (startRow == -1) {
                return result;
            }

            if (includeHistory ) {
                result.addAll(addSinglePrototype2(inputData, prototype, startRow, startColumn));
            } else {
                if(result.isEmpty()) {
                    List<Prototype> stepsInAddingPrototype = addSinglePrototype2(inputData, prototype, startRow, startColumn);
                    result.add(stepsInAddingPrototype.get(stepsInAddingPrototype.size() - 1));
                }
                else {
                    result.addAll(addSinglePrototype2(inputData, prototype, startRow, startColumn));
                }
            }
            ++current;
        }
    }


    public List<Prototype> addSinglePrototype2(boolean inputData[][], Collection<AngleLine> prototype, int initialRowOffset, int initialColumnOffset) {
        int numberOfRows = inputData.length;
        int numberOfColumns = inputData[0].length;
        int[][] distanceMap = MatchDistance.computeDistanceMap(inputData);
        boolean[][] occupiedData = new boolean[numberOfRows][numberOfColumns];
        AngleLine originalFirst = new AngleLine(prototype.iterator().next());

        AngleLine shiftedFirst = new AngleLine(originalFirst);
        shiftedFirst.setStartPair(Pair.of(originalFirst.getStartPair().getRow() + initialRowOffset,
                originalFirst.getStartPair().getColumn() + initialColumnOffset));

        // Fit line segment in prototype
        List<kotlin.Pair<AngleLine, Integer>> linePrototypeIntegerPair = fitSingleLinePrototype(shiftedFirst, distanceMap, occupiedData, numberOfRows, numberOfColumns);
        linePrototypeIntegerPair.add(0, new kotlin.Pair<>(shiftedFirst, 0));

        // Move the line into position
        List<List<AngleLineMoveOperation>> moveOperations = new ArrayList<>();
        AngleLine previousPrototype = new AngleLine(originalFirst);
        for (kotlin.Pair<AngleLine, Integer> pair : linePrototypeIntegerPair) {
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
            collect.add(new PrototypeCollection<>(linesToAdd));
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


    private static List<AngleLineMoveOperation> computeMovements(AngleLine originalPrototype,
                                                                 AngleLine processedPrototype) {
        // TODO An assumption is made here that each prototype only has one segment
        Pair originalStartPair = originalPrototype.getSegments().get(0).getPairs().get(0);
        Pair processedStartPair = processedPrototype.getSegments().get(0).getPairs().get(0);


        int rowShift = processedStartPair.getRow() - originalStartPair.getRow();
        int columnShift = processedStartPair.getColumn() - originalStartPair.getColumn();

        double deltaLength = processedPrototype.getLength() - originalPrototype.getLength();
        double rotationAngle = processedPrototype.getAngle() - originalPrototype.getAngle();

        LOG.info("Rotation angle: {}", rotationAngle);

        return Collections.singletonList(new AngleLineMoveOperationImpl(rowShift, columnShift, deltaLength, rotationAngle));
    }


    private static <T extends AdjustablePrototype> List<kotlin.Pair<T, Integer>> fitSingleLinePrototype(T linePrototype, int distanceMap[][],
                                                                                                        boolean occupiedData[][],
                                                                                                        int numberOfRows, int numberOfColumns) {
        int scoreUnchanged = 0;
        int bestScore = computeScore2(linePrototype.getSegments().get(0).getPairs(), distanceMap, occupiedData);
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

                        // If the end points are valid, then all the points in between have to be valid
                        if (!validCoordinates(startPair, numberOfRows, numberOfColumns)
                                || !validCoordinates(endPair, numberOfRows, numberOfColumns)) {
                            return null;
                        }

                        return new kotlin.Pair<>((T) linePrototype1,
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

                    if(pair.getRow() >= distanceMatrix.length || pair.getColumn() >= distanceMatrix[0].length) {
                        LOG.warn("Point is outside matrix. Distance matrix dimensions: " +distanceMatrix.length +", " +distanceMatrix[0].length +". Pair: " +pair);
                        return Integer.MIN_VALUE;
                    }

                    int distance = distanceMatrix[pair.getRow()][pair.getColumn()];

                    LOG.debug("Distance: " + distance);

                    if (distance > 0) {
                        score += -distance;
                    } else {
                        score += 10;
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
