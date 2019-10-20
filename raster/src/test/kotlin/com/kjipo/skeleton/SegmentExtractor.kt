package com.kjipo.skeleton

import com.kjipo.raster.EncodingUtilities
import com.kjipo.raster.FlowDirection
import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.Matrix
import com.kjipo.visualization.RasterVisualizer2
import javafx.scene.paint.Color
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.nio.file.Paths


fun extractSegments(image: Matrix<Boolean>) {
    val thinnedImage = makeThin(image)
    val segmentImage = Matrix(thinnedImage.numberOfRows, thinnedImage.numberOfColumns, {row, column -> 0})
    val endPoints = bwmorphEndpoints(thinnedImage)

    val endPointCoordinates = mutableListOf<Pair<Int, Int>>()
    endPoints.forEachIndexed({ row, column, value ->
        if (value) {
            endPointCoordinates.add(Pair(row, column))
        }
    })


    var segmentCounter = 1
    endPointCoordinates.forEach {
        if(!thinnedImage[it.first, it.second]) {
            // Point has already been processed
            return@forEach
        }
        val segment = findSegment(it.first, it.second, it.first, it.second, thinnedImage)


        println("Segment length: ${segment.size}")

        segment.forEach {
            thinnedImage[it.first, it.second] = false
            segmentImage[it.first, it.second] = segmentCounter
        }

        ++segmentCounter
    }


    println("Number of segments: $segmentCounter")



    val colorImage = Array(image.numberOfRows, { row ->
        Array(image.numberOfColumns, { column ->
            if(segmentImage[row, column] == 0) {
                Color.BLACK
            }
            else {
                Color.hsb(segmentImage[row, column].toDouble().div(segmentCounter).times(360), 1.0, 1.0)
            }
        })
    })

    RasterVisualizer2.paintRaster(colorImage, 5)

    Thread.sleep(Long.MAX_VALUE)


}


fun findSegment(startRow: Int, startColumn: Int, previousRow: Int, previousColumn: Int, image: Matrix<Boolean>): List<Pair<Int, Int>> {
    val segmentElements = mutableListOf<Pair<Int, Int>>()
    var prevRow = previousRow
    var prevColumn = previousColumn
    var currentRow = startRow
    var currentColumn = startColumn

    while(true) {
        val nextDirection = FlowDirection.values().filter {
            EncodingUtilities.validCell(currentRow, currentColumn, it, image.numberOfRows, image.numberOfColumns)
        }
                .filter { currentRow.plus(it.rowShift) != prevRow || currentColumn.plus(it.columnShift) != prevColumn }
                .filter { image[currentRow.plus(it.rowShift), currentColumn.plus(it.columnShift)] }
                .toList()

        if (nextDirection.isEmpty()) {
            return segmentElements
        }

        prevRow = currentRow
        prevColumn = currentColumn
        if(nextDirection.size == 1) {
            currentRow += nextDirection[0].rowShift
            currentColumn += nextDirection[0].columnShift
            segmentElements.add(Pair(currentRow, currentColumn))
        }
        else {
            // More than one possible direction
            return segmentElements
        }

    }


}


fun main(args: Array<String>) {
    val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
            .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

    val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, { row, column -> encodedKanji.image[row][column] })

    extractSegments(image)

}