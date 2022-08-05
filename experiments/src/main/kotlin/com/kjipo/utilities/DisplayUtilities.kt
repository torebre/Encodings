package com.kjipo.utilities

import com.kjipo.representation.prototype.AngleLine
import com.kjipo.segmentation.Matrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.displayColourRasters
import javafx.scene.paint.Color

object DisplayUtilities {

    fun displayLinesUsingColourPalette(kanjiSegmentedInLines: List<List<AngleLine>>, texts: List<String>, numberOfRows: Int, numberOfColumns: Int) {
        val colourRasters = mutableListOf<Array<Array<Color>>>()

        kanjiSegmentedInLines.forEach { lines ->
            val dispImage = Matrix(numberOfRows, numberOfColumns) { _, _ ->
                Color.BLACK
            }
            var counter = 0
            lines.forEach { line ->
                line.segments.flatMap { it.pairs }.forEach {
                    with(it) {
                        if (row >= 0 && row < dispImage.numberOfRows && column >= 0 && column < dispImage.numberOfColumns) {
                            if (dispImage[row, column].brightness == 1.0) {
                                dispImage[row, column] = Color.WHITE
                            } else {
                                dispImage[row, column] = Color.hsb(counter.toDouble().div(lines.size).times(360), 1.0, 1.0)
                            }
                        }
                    }
                }
                ++counter
            }
            colourRasters.add(transformToArrays(dispImage))
        }

        displayColourRasters(colourRasters, texts, 2)
    }

}
