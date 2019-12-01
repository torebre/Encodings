package com.kjipo.representation.segmentation

import com.kjipo.representation.raster.FlowDirection


//class FitPrototype {
//    fun addPrototypes(inputData: Array<BooleanArray>, prototype: Collection<com.kjipo.prototype.AngleLine?>, includeHistory: Boolean): List<Prototype> {
//        val result: MutableList<Prototype> = java.util.ArrayList<Prototype>()
//        if (includeHistory) {
//            result.addAll(addSinglePrototype2(inputData, prototype, 0, 0))
//        } else {
//            if (result.isEmpty()) {
//                val stepsInAddingPrototype: List<Prototype> = addSinglePrototype2(inputData, prototype, 0, 0)
//                result.add(stepsInAddingPrototype[stepsInAddingPrototype.size - 1])
//            } else {
//                result.addAll(addSinglePrototype2(inputData, prototype, 0, 0))
//            }
//        }
//        return result
//    }
//
//    fun addSinglePrototype2(inputData: Array<BooleanArray>, prototype: Collection<com.kjipo.prototype.AngleLine?>, initialRowOffset: Int, initialColumnOffset: Int): List<Prototype> {
//        val numberOfRows = inputData.size
//        val numberOfColumns: Int = inputData[0].length
//        val distanceMap: Array<IntArray> = com.kjipo.raster.match.MatchDistance.computeDistanceMap(inputData)
//        val occupiedData = Array(numberOfRows) { BooleanArray(numberOfColumns) }
//        val originalFirst: com.kjipo.prototype.AngleLine = com.kjipo.prototype.AngleLine(prototype.iterator().next())
//        val shiftedFirst: com.kjipo.prototype.AngleLine = com.kjipo.prototype.AngleLine(originalFirst)
//        shiftedFirst.setStartPair(com.kjipo.raster.segment.Pair.of(originalFirst.getStartPair().getRow() + initialRowOffset,
//                originalFirst.getStartPair().getColumn() + initialColumnOffset))
//        // Fit line segment in prototype
//        val fittedPrototypes: List<Pair<com.kjipo.prototype.AngleLine, Int>> = fitSingleLinePrototype<com.kjipo.prototype.AngleLine>(shiftedFirst, distanceMap, numberOfRows, numberOfColumns)
//        val angleLineIntegerPair: Pair<com.kjipo.prototype.AngleLine, Int> = fittedPrototypes[fittedPrototypes.size - 1]
//        LOG.info("Best fit: $angleLineIntegerPair")
//        // TODO Use defined constant
//        return if (angleLineIntegerPair.second < -500) {
//            emptyList()
//        } else com.google.common.collect.Lists.newArrayList(angleLineIntegerPair.component1())
//        // TODO Code commented out to make it easier to see what happens when adding single line prototype
//        /*
//
//        List<kotlin.Pair<AngleLine, Integer>> startEndPrototype = Lists.newArrayList(new kotlin.Pair<>(shiftedFirst, 0), fittedPrototypes.get(fittedPrototypes.size() - 1));
//
//        // Move the line into position
//        List<List<AngleLineMoveOperation>> moveOperations = new ArrayList<>();
//        AngleLine previousPrototype = new AngleLine(originalFirst);
//        for (kotlin.Pair<AngleLine, Integer> pair : startEndPrototype) {
//            moveOperations.add(computeMovements(previousPrototype, pair.getFirst()));
//            previousPrototype = pair.getFirst();
//        }
//
//        // Add the lines in the prototype to a list in the
//        // order they need to be processed by looking at
//        // which lines are connected
//        List<Integer> processedLines = new ArrayList<>();
//        while (processedLines.size() < prototype.size()) {
//            for (AngleLine angleLine : prototype) {
//                if (!processedLines.contains(angleLine.getId())
//                        && processedLines.containsAll(angleLine.getConnectedTo())) {
//                    processedLines.add(angleLine.getId());
//                }
//            }
//        }
//
//        // Create a mapping between the ID of the lines and the lines in the prototype
//        Map<Integer, AngleLine> idLineMap = prototype.stream()
//                .collect(Collectors.toMap(AngleLine::getId, AngleLine::new));
//
//        List<AngleLine> iterationOrder = processedLines.stream()
//                .map(idLineMap::get)
//                .collect(Collectors.toList());
//        Collections.reverse(iterationOrder);
//
//        iterationOrder.forEach(angleLine -> angleLine.getConnectedTo().stream()
//                .map(idLineMap::get)
//                .forEach(connectedTo -> {
//                    connectedTo.setStartPair(angleLine.getEndPair());
//                    connectedTo.addAngleOffset(angleLine.getAngle() + angleLine.getAngleOffset());
//                }));
//
//
//        // Make a copy of the prototype
//        List<Prototype> collect = new ArrayList<>();
//        List<AngleLine> originalConfiguration = iterationOrder.stream().map(AngleLine::new).collect(Collectors.toList());
//        collect.add(new PrototypeCollection<>(originalConfiguration));
//
//        // Apply move operations to all segments in prototype
//        for (List<AngleLineMoveOperation> moveOperation : moveOperations) {
//            for (AngleLineMoveOperation lineMoveOperation : moveOperation) {
//                boolean first = true;
//                for (AngleLine angleLine : iterationOrder) {
//                    if (first) {
//                        lineMoveOperation.apply(angleLine);
//                        first = false;
//                    } else {
//                        lineMoveOperation.applyStretching(angleLine);
//                    }
//                    angleLine.getConnectedTo().stream()
//                            .map(idLineMap::get)
//                            .forEach(angleLine1 -> {
//                                angleLine1.setStartPair(angleLine.getEndPair());
//                                angleLine1.addAngleOffset(lineMoveOperation.getRotation());
//                            });
//                }
//            }
//
//            List<AngleLine> linesToAdd = iterationOrder.stream().map(AngleLine::new).collect(Collectors.toList());
//
//            // TODO Only here for debugging
//            for (AngleLine angleLine : linesToAdd) {
//                List<Segment> segments = angleLine.getSegments();
//                for (Segment segment : segments) {
//                    for (Pair pair : segment.getPairs()) {
//                        if(pair.getRow() < 0 || pair.getRow() >= inputData.length || pair.getColumn() < 0 || pair.getColumn() > inputData[0].length) {
//                            System.out.println("Test30");
//                        }
//
//                    }
//
//                }
//
//
//            }
//
//            collect.add(new PrototypeCollection<>(linesToAdd));
//        }
//
//        return collect;
//
//        */
//    }
//
//    companion object {
//        private const val MAX_ITERATIONS = 1000
//        private val LOG: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(FitPrototype::class.java)
//        fun applyMoveOperations(prototype: Prototype, moveOperations: Collection<LineMoveOperation>): List<Prototype> {
//            var prototype: Prototype = prototype
//            val moves: MutableList<Prototype> = java.util.ArrayList<Prototype>()
//            moves.add(prototype)
//            for (lineMoveOperation in moveOperations) {
//                prototype = PrototypeImpl(moves[moves.size - 1].getSegments().stream()
//                        .map(java.util.function.Function<com.kjipo.raster.segment.Segment, com.kjipo.raster.segment.Segment> { linePrototype: com.kjipo.raster.segment.Segment? -> lineMoveOperation.applyToLine(linePrototype) })
//                        .collect(Collectors.toList()))
//                moves.add(prototype)
//            }
//            return moves
//        }
//
//        private fun computeMovements(originalPrototype: com.kjipo.prototype.AngleLine,
//                                     processedPrototype: com.kjipo.prototype.AngleLine): List<AngleLineMoveOperation> { // TODO An assumption is made here that each prototype only has one segment
//            val originalStartPair: com.kjipo.raster.segment.Pair = originalPrototype.getSegments().get(0).getPairs().get(0)
//            val processedStartPair: com.kjipo.raster.segment.Pair = processedPrototype.getSegments().get(0).getPairs().get(0)
//            val rowShift: Int = processedStartPair.getRow() - originalStartPair.getRow()
//            val columnShift: Int = processedStartPair.getColumn() - originalStartPair.getColumn()
//            val deltaLength: Double = processedPrototype.getLength() - originalPrototype.getLength()
//            val rotationAngle: Double = processedPrototype.getAngle() - originalPrototype.getAngle()
//            //        LOG.info("Rotation angle: {}", rotationAngle);
//            return listOf(AngleLineMoveOperationImpl(rowShift, columnShift, deltaLength, rotationAngle))
//        }
//
//        private fun <T : AdjustablePrototype?> fitSingleLinePrototype(linePrototype: T, distanceMap: Array<IntArray>,
//                                                                      numberOfRows: Int, numberOfColumns: Int): List<Pair<T, Int>> {
//            var scoreUnchanged = 0
//            var bestScore = computeScore3(linePrototype.getSegments().get(0).getPairs(), distanceMap)
//            var previousBestScore = bestScore
//            val priorityQueue: java.util.PriorityQueue<Pair<T, Int>> = java.util.PriorityQueue<Pair<T, Int>>(java.util.Comparator.comparingInt(ToIntFunction<Pair<T, Int>> { (_, second) -> -second!! }))
//            val firstPair = Pair(linePrototype, bestScore)
//            val bestScorePairs: MutableList<Pair<T, Int>> = java.util.ArrayList<Pair<T, Int>>()
//            priorityQueue.add(firstPair)
//            bestScorePairs.add(firstPair)
//            val tabooSearchSet: MutableSet<T> = HashSet()
//            for (i in 0 until MAX_ITERATIONS) { // A line prototype only has one segment
//                if (priorityQueue.isEmpty()) {
//                    break
//                }
//                // TODO Mostly for testing to see if the search is able to find a good fit
//                val nextPrototype: Pair<T, Int> = priorityQueue.poll()
//                if (tabooSearchSet.contains(nextPrototype.first)) {
//                    continue
//                } else {
//                    tabooSearchSet.add(nextPrototype.first)
//                }
//                //            LOG.info("Checking prototype: {}", nextPrototype);
//                if (nextPrototype.second > bestScore) { //                LOG.info("New best score: {}", nextPrototype);
//                    previousBestScore = bestScore
//                    bestScore = nextPrototype.second
//                    bestScorePairs.add(nextPrototype)
//                }
//                nextPrototype.component1().getMovements()
//                        .map({ linePrototype1: AdjustablePrototype? ->
//                            val segment1: com.kjipo.raster.segment.Segment = linePrototype1.getSegments().get(0)
//                            for (pair in segment1.getPairs()) {
//                                if (!validCoordinates(pair, numberOfRows, numberOfColumns)) {
//                                    return@map null
//                                }
//                            }
//                            //                        Pair startPair = segment1.getPairs().get(0);
////                        Pair endPair = segment1.getPairs().get(segment1.getPairs().size() - 1);
////                        // If the end points are valid, then all the points in between have to be valid
////                        if (!validCoordinates(startPair, numberOfRows, numberOfColumns)
////                                || !validCoordinates(endPair, numberOfRows, numberOfColumns)) {
////                            return null;
////                        }
//                            val score = computeScore3(segment1.getPairs(),
//                                    distanceMap)
//                            Pair(linePrototype1 as T?, score)
//                        })
//                        .filter(java.util.function.Predicate<Pair<T, Int>> { obj: Any? -> java.util.Objects.nonNull(obj) })
//                        .sorted(java.util.Comparator.comparing(java.util.function.Function<Pair<T, Int>, Int> { (_, second) -> -second!! })) //                    .limit(5)
//                        .forEach(java.util.function.Consumer<Pair<T, Int>> { e: E? -> priorityQueue.add(e) })
//                if (previousBestScore == bestScore) {
//                    if (scoreUnchanged == 50) {
//                        break
//                    }
//                    ++scoreUnchanged
//                } else {
//                    scoreUnchanged = 0
//                }
//            }
//            //        LOG.info("Best score: {}", bestScore);
//            return bestScorePairs
//        }
//
//        private fun computeScore(pairs: List<com.kjipo.raster.segment.Pair>, distanceMatrix: Array<IntArray>, occupiedMatrix: Array<BooleanArray>): Int {
//            return pairs.stream()
//                    .mapToInt(ToIntFunction<com.kjipo.raster.segment.Pair> { pair: com.kjipo.raster.segment.Pair ->
//                        var score = 0
//                        val distance = distanceMatrix[pair.getRow()][pair.getColumn()]
//                        score += if (distance > 0) {
//                            -distance
//                        } else {
//                            1
//                        }
//                        if (occupiedMatrix[pair.getRow()][pair.getColumn()]) {
//                            score += -50
//                        }
//                        score
//                    }).sum()
//        }
//
//        private fun computeScore2(pairs: List<com.kjipo.raster.segment.Pair>, distanceMatrix: Array<IntArray>, occupiedMatrix: Array<BooleanArray>): Int {
//            val startPair: com.kjipo.raster.segment.Pair = pairs[0]
//            val stopPair: com.kjipo.raster.segment.Pair = pairs[pairs.size - 1]
//            if (startPair.getRow() < 0 || startPair.getColumn() >= distanceMatrix[0].length || stopPair.getRow() < 0 || stopPair.getColumn() >= distanceMatrix[0].length) {
//                return Int.MIN_VALUE
//            }
//            var lengthScore = 0
//            if (distanceMatrix[startPair.getRow()][startPair.getColumn()] == 0
//                    && distanceMatrix[stopPair.getRow()][stopPair.getColumn()] == 0) {
//                lengthScore = pairs.size
//                LOG.debug("Length score: $lengthScore")
//            }
//            return pairs.stream()
//                    .mapToInt(ToIntFunction<com.kjipo.raster.segment.Pair> { pair: com.kjipo.raster.segment.Pair ->
//                        var score = 0
//                        if (pair.getRow() >= distanceMatrix.size || pair.getColumn() >= distanceMatrix[0].length) {
//                            LOG.warn("Point is outside matrix. Distance matrix dimensions: " + distanceMatrix.size + ", " + distanceMatrix[0].length + ". Pair: " + pair)
//                            return@mapToInt Int.MIN_VALUE
//                        }
//                        val distance = distanceMatrix[pair.getRow()][pair.getColumn()]
//                        LOG.debug("Distance: $distance")
//                        score += if (distance > 0) {
//                            -distance
//                        } else {
//                            1
//                        }
//                        score
//                    }).sum()
//            //                + lengthScore;
//        }
//
//        private fun computeScore3(pairs: List<com.kjipo.raster.segment.Pair>, distanceMatrix: Array<IntArray>): Int {
//            val startPair: com.kjipo.raster.segment.Pair = pairs[0]
//            val stopPair: com.kjipo.raster.segment.Pair = pairs[pairs.size - 1]
//            if (startPair.getRow() < 0 || startPair.getColumn() >= distanceMatrix[0].length || stopPair.getRow() < 0 || stopPair.getColumn() >= distanceMatrix[0].length) {
//                return -1000
//            }
//            var score = 0
//            for (pair in pairs) {
//                if (pair.getRow() < 0 || pair.getRow() >= distanceMatrix.size || pair.getColumn() < 0 || pair.getColumn() >= distanceMatrix[0].length) {
//                    LOG.warn("Point is outside matrix. Distance matrix dimensions: " + distanceMatrix.size + ", " + distanceMatrix[0].length + ". Pair: " + pair)
//                    return -1000
//                }
//                val distance = distanceMatrix[pair.getRow()][pair.getColumn()]
//                LOG.debug("Distance: $distance")
//                score += if (distance > 0) {
//                    return -1000
//                } else {
//                    1
//                }
//            }
//            return score
//        }
//
//        fun validCoordinates(row: Int, column: Int, numberOfRows: Int, numberOfColumns: Int): Boolean {
//            return row >= 0 && row < numberOfRows && column >= 0 && column < numberOfColumns
//        }
//
//        fun validCoordinates(pair: com.kjipo.raster.segment.Pair, numberOfRows: Int, numberOfColumns: Int): Boolean {
//            return pair.getRow() >= 0 && pair.getRow() < numberOfRows && pair.getColumn() >= 0 && pair.getColumn() < numberOfColumns
//        }
//
//        private fun computeOccupied(prototypes: Collection<Prototype>, rows: Int, columns: Int): Array<BooleanArray> {
//            val result = Array(rows) { BooleanArray(columns) }
//            prototypes.stream()
//                    .flatMap(java.util.function.Function<Prototype, java.util.stream.Stream<out com.kjipo.raster.segment.Segment?>> { prototype: Prototype -> prototype.getSegments().stream() })
//                    .flatMap(java.util.function.Function<com.kjipo.raster.segment.Segment, java.util.stream.Stream<out com.kjipo.raster.segment.Pair?>> { segment: com.kjipo.raster.segment.Segment -> segment.getPairs().stream() })
//                    .forEach(java.util.function.Consumer<com.kjipo.raster.segment.Pair> { pair: com.kjipo.raster.segment.Pair -> result[pair.getRow()][pair.getColumn()] = false })
//            return result
//        }
//
//        private fun nextStart(inputData: Array<BooleanArray>, occupiedData: Array<BooleanArray>): LinePrototype? {
//            for (row in inputData.indices) {
//                for (column in 0 until inputData[0].length) {
//                    if (inputData[row][column]
//                            && !occupiedData[row][column]) {
//                        return LinePrototype(com.kjipo.raster.segment.Pair.of(row, column), com.kjipo.raster.segment.Pair.of(row, column))
//                    }
//                }
//            }
//            return null
//        }
//
//        private fun nextStartPair(inputData: Array<BooleanArray>, occupiedData: Array<BooleanArray>): com.kjipo.raster.segment.Pair? {
//            for (row in inputData.indices) {
//                for (column in 0 until inputData[0].length) {
//                    if (inputData[row][column]
//                            && !occupiedData[row][column]) {
//                        return com.kjipo.raster.segment.Pair.of(row, column)
//                    }
//                }
//            }
//            return null
//        }
//
//        private fun nextStartInRegion(inputData: Array<BooleanArray>, occupiedData: Array<BooleanArray>,
//                                      regionData: Array<IntArray>, regionValue: Int): LinePrototype? {
//            for (row in inputData.indices) {
//                for (column in 0 until inputData[0].length) {
//                    if (inputData[row][column]
//                            && !occupiedData[row][column]
//                            && regionData[row][column] == regionValue) {
//                        return LinePrototype(com.kjipo.raster.segment.Pair.of(row, column), com.kjipo.raster.segment.Pair.of(row, column))
//                    }
//                }
//            }
//            return null
//        }
//
//        fun findDisjointRegions(inputData: Array<BooleanArray>): Array<IntArray> {
//            val regionData = Array(inputData.size) { IntArray(inputData[0].length) }
//            var fillValue = 1
//            var foundHit = true
//            while (foundHit) {
//                foundHit = false
//                for (row in inputData.indices) {
//                    for (column in 0 until inputData[0].length) {
//                        if (inputData[row][column]
//                                && regionData[row][column] == 0) {
//                            spreadAcrossRegion(row, column, fillValue++, inputData, regionData)
//                            foundHit = true
//                        }
//                        if (foundHit) {
//                            break
//                        }
//                    }
//                    if (foundHit) {
//                        break
//                    }
//                }
//            }
//            return regionData
//        }
//
//        fun findNumberOfDisjointRegions(inputData: Array<BooleanArray>): Int {
//            val regionData = Array(inputData.size) { IntArray(inputData[0].length) }
//            var fillValue = 1
//            var foundHit = true
//            while (foundHit) {
//                foundHit = false
//                for (row in inputData.indices) {
//                    for (column in 0 until inputData[0].length) {
//                        if (inputData[row][column]
//                                && regionData[row][column] == 0) {
//                            spreadAcrossRegion(row, column, fillValue++, inputData, regionData)
//                            foundHit = true
//                        }
//                        if (foundHit) {
//                            break
//                        }
//                    }
//                    if (foundHit) {
//                        break
//                    }
//                }
//            }
//            return fillValue
//        }
//
//        private fun spreadAcrossRegion(startRow: Int, startColumn: Int, fillValue: Int,
//                                       inputData: Array<BooleanArray>, regionData: Array<IntArray>) {
//            val cellsToVisit: Deque<com.kjipo.raster.segment.Pair> = ArrayDeque<com.kjipo.raster.segment.Pair>()
//            cellsToVisit.add(com.kjipo.raster.segment.Pair.of(startRow, startColumn))
//            while (!cellsToVisit.isEmpty()) {
//                val pair: com.kjipo.raster.segment.Pair = cellsToVisit.poll()
//                val row: Int = pair.getRow()
//                val column: Int = pair.getColumn()
//                if (com.kjipo.raster.EncodingUtilities.validCoordinates(row, column, inputData.size, inputData[0].length)
//                        && inputData[row][column]) {
//                    regionData[row][column] = fillValue
//                }
//                for (flowDirection in FlowDirection.values()) {
//                    val nextRow = row + flowDirection.rowShift
//                    val nextColumn = column + flowDirection.columnShift
//                    val nextPair: com.kjipo.raster.segment.Pair = com.kjipo.raster.segment.Pair.of(nextRow, nextColumn)
//                    if (com.kjipo.raster.EncodingUtilities.validCoordinates(nextRow, nextColumn, inputData.size, inputData[0].length)
//                            && inputData[nextRow][nextColumn]
//                            && regionData[nextRow][nextColumn] == 0 && !cellsToVisit.contains(nextPair)) {
//                        cellsToVisit.add(nextPair)
//                    }
//                }
//            }
//        }
//    }
//}
