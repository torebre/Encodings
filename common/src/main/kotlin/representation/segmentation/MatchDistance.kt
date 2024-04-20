package com.kjipo.representation.segmentation

//import com.kjipo.representation.raster.FlowDirection
//import com.kjipo.representation.raster.TileType
//
//object MatchDistance {
//    private val LOG: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(MatchDistance::class.java)
//    fun computeDistanceMap(prototype: Array<BooleanArray>): Array<IntArray> {
//        val cellsToProcessNext: java.util.Queue<com.kjipo.raster.segment.Pair> = ArrayDeque<com.kjipo.raster.segment.Pair>()
//        val distanceMap = Array(prototype.size) { IntArray(prototype[0].length) }
//        for (row in distanceMap) {
//            java.util.Arrays.fill(row, -1)
//        }
//        // First pass. Mark the pixels that are part of the image, these have a distance of 0
//        for (row in prototype.indices) {
//            for (column in 0 until prototype[0].length) {
//                if (prototype[row][column]) {
//                    distanceMap[row][column] = 0
//                    val tileTypes: Array<TileType> = com.kjipo.raster.EncodingUtilities.determineNeighbourTypes(row, column, prototype)
//                    for (tileType in tileTypes) {
//                        if (tileType === TileType.OUTSIDE_CHARACTER) {
//                            val pair: com.kjipo.raster.segment.Pair = com.kjipo.raster.segment.Pair(row, column)
//                            if (!cellsToProcessNext.contains(pair)) {
//                                cellsToProcessNext.add(pair)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        var distance = 1
//        val nextBatch: MutableList<com.kjipo.raster.segment.Pair> = java.util.ArrayList<com.kjipo.raster.segment.Pair>()
//        do {
//            while (!cellsToProcessNext.isEmpty()) {
//                val next: com.kjipo.raster.segment.Pair = cellsToProcessNext.poll()
//                val tileTypes: Array<TileType> = com.kjipo.raster.EncodingUtilities.determineNeighbourTypes(next.getRow(), next.getColumn(), prototype)
//                val flowDirections = FlowDirection.values()
//                var counter = 0
//                for (tileType in tileTypes) {
//                    if (tileType === TileType.OUTSIDE_CHARACTER
//                            && distanceMap[next.getRow() + flowDirections[counter].rowShift][next.getColumn() + flowDirections[counter].columnShift] == -1) {
//                        nextBatch.add(com.kjipo.raster.segment.Pair(next.getRow() + flowDirections[counter].rowShift,
//                                next.getColumn() + flowDirections[counter].columnShift))
//                        distanceMap[next.getRow() + flowDirections[counter].rowShift][next.getColumn() + flowDirections[counter].columnShift] = distance
//                    }
//                    ++counter
//                }
//            }
//            cellsToProcessNext.addAll(nextBatch)
//            nextBatch.clear()
//            ++distance
//        } while (!cellsToProcessNext.isEmpty())
//        return distanceMap
//    }
//
//    fun computeDistanceBasedOnDistanceMap(raster: Array<BooleanArray>, distanceMap: Array<IntArray>): Int {
//        var totalDistance = 0
//        for (row in raster.indices) {
//            for (column in 0 until raster[0].length) {
//                if (raster[row][column]) {
//                    totalDistance += distanceMap[row][column]
//                }
//            }
//        }
//        return totalDistance
//    }
//}