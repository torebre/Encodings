package com.kjipo.segmentation

import com.google.common.collect.ImmutableList
import com.kjipo.representation.EncodedKanji
import com.kjipo.visualization.CellType
import com.kjipo.visualization.RasterElementProcessor
import com.kjipo.visualization.RasterRun
import com.kjipo.visualization.RasterVisualizer2
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.paint.Color
import org.junit.Test
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths


private val logger = LoggerFactory.getLogger("LineSegmentationTest")

fun segmentKanji() {
    val encodedKanji = Files.newInputStream(Paths.get("/home/student/test_kanji.xml")).use {
        java.io.ObjectInputStream(it).use {
            it.readObject()
        }
    } as EncodedKanji

    val matrix = Matrix(encodedKanji.image.size, encodedKanji.image[0].size,
            encodedKanji.image.map { it.toTypedArray() }.toTypedArray())
    val expandedLine = expandLine(encodedKanji.image)


    showRasterFlow(encodedKanji.image, matrix, expandedLine)

}


@Throws(InterruptedException::class)
private fun showRasterFlow(rasterInput: Array<BooleanArray>,
                           encodedKanji: Matrix<Boolean>,
                           segment: List<Pair<Int, Int>>) {
    RasterVisualizer2.showRasterFlow(
            object : RasterRun<ColorCell2> {
                private var current = 0

                override fun getRawInput(): Array<BooleanArray> {
                    return rasterInput
                }

                override fun hasNext(): Boolean {
                    return current < 1
                }

                override fun getColumns(): Int {
                    return encodedKanji.numberOfColumns
                }

                override fun getRows(): Int {
                    return encodedKanji.numberOfRows
                }

                override fun getCell(row: Int, column: Int): ColorCell2 {
                    val color = if(segment.contains(Pair(row, column))) {
                        Color.RED
                    }
                    else if(encodedKanji[row, column] == true) {
                        Color.GREEN
                    }
                    else {
                        Color.BLACK
                    }
                    return ColorCell2(row, column, color)
                }

                override fun next() {
                    ++current
                }
            },
            ImmutableList.of<RasterElementProcessor<ColorCell2>>(ColorPainter2()))

}


data class ColorCell2(val row: Int, val column: Int, val colour: Color) : CellType


class ColorPainter2 : RasterElementProcessor<ColorCell2> {

    override fun processCell(cell: ColorCell2, squareSize: Int, node: ObservableList<Node>, rectangle: javafx.scene.shape.Rectangle) {
        rectangle.fill = cell.colour
    }
}


fun segmentKanji2() {
    val encodedKanji = Files.newInputStream(Paths.get("/home/student/test_kanji.xml")).use {
        java.io.ObjectInputStream(it).use {
            it.readObject()
        }
    } as EncodedKanji

    val matrix = Matrix(encodedKanji.image.size, encodedKanji.image[0].size,
            encodedKanji.image.map { it.toTypedArray() }.toTypedArray())

    val expandedLine = traceSegments(encodedKanji.image)

    showRasterFlow(encodedKanji.image, matrix, expandedLine)
}


fun computeLineTest() {
    computeLine(Pair(0, 3), Pair(0, 1)).forEach(System.out::println)

}



fun main(args: Array<String> ) {
//    segmentKanji2()
    computeLineTest()

}