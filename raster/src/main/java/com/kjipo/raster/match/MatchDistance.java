package com.kjipo.raster.match;

import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.segment.Pair;
import com.kjipo.representation.raster.FlowDirection;
import com.kjipo.representation.raster.TileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MatchDistance {

    private static final Logger LOG = LoggerFactory.getLogger(MatchDistance.class);


    public static int[][] computeDistanceMap(boolean prototype[][]) {
        Queue<Pair> cellsToProcessNext = new ArrayDeque<>();
        int distanceMap[][] = new int[prototype.length][prototype[0].length];

        for (int row[] : distanceMap) {
            Arrays.fill(row, -1);
        }

        // First pass. Mark the pixels that are part of the image, these have a distance of 0
        for (int row = 0; row < prototype.length; ++row) {
            for (int column = 0; column < prototype[0].length; ++column) {
                if (prototype[row][column]) {
                    distanceMap[row][column] = 0;
                    TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(row, column, prototype);

                    for (TileType tileType : tileTypes) {
                        if (tileType == TileType.OUTSIDE_CHARACTER) {
                            Pair pair = new Pair(row, column);

                            if (!cellsToProcessNext.contains(pair)) {
                                cellsToProcessNext.add(pair);
                            }

                        }
                    }
                }
            }
        }


        int distance = 1;
        List<Pair> nextBatch = new ArrayList<>();

        do {
            while (!cellsToProcessNext.isEmpty()) {
                Pair next = cellsToProcessNext.poll();
                TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(next.getRow(), next.getColumn(), prototype);
                FlowDirection flowDirections[] = FlowDirection.values();
                int counter = 0;

                for (TileType tileType : tileTypes) {
                    if (tileType == TileType.OUTSIDE_CHARACTER
                            && distanceMap[next.getRow() + flowDirections[counter].getRowShift()][next.getColumn() + flowDirections[counter].getColumnShift()] == -1) {
                        nextBatch.add(new Pair(next.getRow() + flowDirections[counter].getRowShift(),
                                next.getColumn() + flowDirections[counter].getColumnShift()));
                        distanceMap[next.getRow() + flowDirections[counter].getRowShift()][next.getColumn() + flowDirections[counter].getColumnShift()] = distance;
                    }
                    ++counter;
                }
            }

            cellsToProcessNext.addAll(nextBatch);
            nextBatch.clear();
            ++distance;

        } while (!cellsToProcessNext.isEmpty());


        return distanceMap;
    }

    public static int computeDistanceBasedOnDistanceMap(boolean raster[][], int distanceMap[][]) {
        int totalDistance = 0;

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (raster[row][column]) {
                    totalDistance += distanceMap[row][column];
                }
            }
        }

        return totalDistance;
    }

}