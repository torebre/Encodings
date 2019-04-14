package com.kjipo.experiments

import com.google.common.collect.ImmutableList
import com.kjipo.prototype.AngleLine
import com.kjipo.prototype.Prototype
import com.kjipo.raster.EncodingUtilities
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.fitSingleLineUsingDevianceMeasure
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.makeThin
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.*
import com.kjipo.visualization.segmentation.ColorCell
import com.kjipo.visualization.segmentation.ColorPainter
import javafx.scene.paint.Color
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.streams.toList


object FitMultipleLinesUsingDevianceMeasure {

    private fun fitLine() {
//        val readImage = ImageIO.read(File("test2.png"))
//        val image = Matrix(readImage.height, readImage.width, { row, column -> false })
//        for (row in 0 until readImage.height) {
//            for (column in 0 until readImage.width) {
//                if (readImage.getRGB(column, row) != -1) {
//                    image[row, column] = true
//                }
//            }
//        }

        val image = loadKanjisFromDirectory(Paths.get("kanji_output8"))
                .filter { encodedKanji ->
                    encodedKanji.unicode == 25022
                }
                .map { encodedKanji -> transformArraysToMatrix(encodedKanji.image) }
                .first()


        val shrinkImage = makeThin(shrinkImage(image, 64, 64))
        val fittedPrototypes = mutableListOf<AngleLine>()

        val tempImage = Matrix.copy(shrinkImage)
        var numberOfPixels = 0
        tempImage.forEach {
            if (it) {
                ++numberOfPixels
            }
        }
        var filledFixels = 0

        while (filledFixels != numberOfPixels) {
            var startPair = Pair(0, 0)
            tempImage.forEachIndexed { row, column, value ->
                if (value) {
                    startPair = Pair(row, column)
                    return@forEachIndexed
                }
            }

            val fittedLine = fitSingleLineUsingDevianceMeasure(tempImage, startPair)

            fittedLine.second.forEach {
                if (tempImage[it.first, it.second]) {
                    ++filledFixels
                }
                tempImage[it.first, it.second] = false
            }

            fittedPrototypes.add(fittedLine.first)
        }


        val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
            if (shrinkImage[row, column]) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        })

        var counter = 0
        fittedPrototypes.forEach {
            it.segments.flatMap { it.pairs }.forEach {
                if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
                    dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(fittedPrototypes.size).times(360), 1.0, 1.0)
                }
            }
            ++counter
        }

        displayColourMatrix(dispImage, 5)






    }


    private fun showRaster(encodedKanji: Array<BooleanArray>, prototypeDevelopment: List<Collection<Prototype>>) {
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


    @JvmStatic
    fun main(args: Array<String>) {
        fitLine()
    }


}