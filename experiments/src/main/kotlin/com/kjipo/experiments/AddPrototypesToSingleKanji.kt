package com.kjipo.experiments

import com.google.common.collect.ImmutableList
import com.kjipo.representation.prototype.AngleLine
import com.kjipo.representation.prototype.FitPrototype
import com.kjipo.representation.prototype.Prototype
import com.kjipo.representation.segment.Pair
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.skeleton.transformToBooleanArrays
import com.kjipo.visualization.RasterElementProcessor
import com.kjipo.visualization.RasterRun
import com.kjipo.visualization.RasterVisualizer2
import com.kjipo.visualization.loadEncodedKanji
import com.kjipo.visualization.segmentation.ColorCell
import com.kjipo.visualization.segmentation.ColorPainter
import javafx.scene.paint.Color
import java.nio.file.Paths


fun addPrototypesToSingleKanji() {
    val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/26681.dat"))
    val image = transformArraysToMatrix(encodedKanji.image)

    val shrinkImage = shrinkImage(image, 32, 32)

    val topPair = Pair.of(0, 0)
    val topId = 1
    val top = AngleLine(topId, topPair, 3.0, 0.0)

    val rightId = 2
    val right = AngleLine(rightId, Pair.of(0, 3), 3.0, 0.5 * Math.PI)
    top.addConnectedTo(rightId)

    val bottomId = 3
    val bottom = AngleLine(bottomId, Pair.of(3, 3), 3.0, 0.5 * Math.PI)
    right.addConnectedTo(bottomId)

    val leftId = 4
    val left = AngleLine(leftId, Pair.of(3, 0), 3.0, 0.5 * Math.PI)
    bottom.addConnectedTo(leftId)

    val connectorId = 5
    val connector = AngleLine(connectorId, Pair.of(3, 3), 3.0, 1.5 * Math.PI)
    bottom.addConnectedTo(connectorId)

    val underId = 6
    val underLine = AngleLine(underId, Pair.of(6, 3), 3.0, 0.0)
    connector.addConnectedTo(underId)

    val allLines = listOf(top, right, bottom, left, connector, underLine)
    val fitPrototype = FitPrototype()

    val imageAsArrays = transformToBooleanArrays(shrinkImage)
    val prototypes = fitPrototype.addPrototypes(imageAsArrays, allLines, false).stream()
            .map { listOf(it) }
            .toList()

    showRaster(imageAsArrays, prototypes)

    Thread.sleep(java.lang.Long.MAX_VALUE)
}


fun showRaster(encodedKanji: Array<BooleanArray>, prototypeDevelopment: List<Collection<Prototype>>) {
    RasterVisualizer2.showRasterFlow(
            object : RasterRun<ColorCell> {
                private var current = -1

                override fun getRawInput(): Array<BooleanArray> {
                    return encodedKanji
                }

                override fun hasNext(): Boolean {
                    return current < prototypeDevelopment.size - 1
                }

                override fun getColumns(): Int {
                    return encodedKanji[0].size
                }

                override fun getRows(): Int {
                    return encodedKanji.size
                }

                override fun getCell(row: Int, column: Int): ColorCell {
                    val pairs = prototypeDevelopment[current].stream()
                            .flatMap { prototype -> prototype.segments.stream() }
                            .flatMap { segment -> segment.pairs.stream() }
                            .toList()

                    return ColorCell(row, column, Color.BLUE, emptyList(), pairs)
                }

                override fun next() {
                    ++current
                }
            },
            ImmutableList.of<RasterElementProcessor<ColorCell>>(ColorPainter()))

}


fun main(args: Array<String>) {
    addPrototypesToSingleKanji()

}