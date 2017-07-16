package com.kjipo.raster.filter;

import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.TileType;

import java.util.Collections;
import java.util.List;

public class SkeletonFilter implements Filter {


    public SkeletonFilter() {

    }


    @Override
    public List<boolean[][]> applyFilter(boolean raster[][]) {
        boolean result[][] = new boolean[raster.length][raster[0].length];

        for (int row = 0; row < raster.length; ++row) {
            for (int column = 0; column < raster[0].length; ++column) {
                if (!raster[row][column]) {
                    continue;
                }

                TileType[] tileTypes = EncodingUtilities.determineNeighbourTypes(row, column, raster);

                boolean includeCell = true;
                for (int i = 0; i < tileTypes.length; ++i) {
                    if (tileTypes[i] != TileType.OPEN) {
                        includeCell = false;
                        break;
                    }
                }
                if (includeCell) {
                    result[row][column] = raster[row][column];
                }
            }
        }

        return Collections.singletonList(result);
    }


}
