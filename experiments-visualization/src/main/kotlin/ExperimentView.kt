package com.kjipo

import javafx.collections.ObservableList
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*


class ExperimentView : View("Experiment view") {
    var rasters: Collection<Array<Array<Color>>> = emptyList()
    val group: Group = Group()

    val rastersPerLine = 20


    fun loadRasters(colourRasters: Collection<Array<Array<Color>>>, texts: List<String> = emptyList(), squareSize: Int = 1) {
        this.rasters = colourRasters
        group.children.clear()
        drawRasters(group.children, texts, squareSize)
    }

    override val root = scrollpane {
        content = group
        drawRasters(group.children, squareSize = 1)
    }

    fun drawRasters(root: ObservableList<Node>, texts: List<String> = emptyList(), squareSize: Int) {
        if (rasters.isEmpty()) {
            return
        }

        var currentRow = 0
        var currentColumn = 0
        var rasterCounter = 0
        val rectangles = Group()

//        val firstRaster = rasters.iterator().next()

        rasters.forEach { colourRaster ->

            if (texts.isNotEmpty()) {
                val text = Text(currentColumn.toDouble() * colourRaster[0].size * squareSize, currentRow.toDouble() * colourRaster.size * squareSize, texts[rasterCounter])
                text.font = Font(20.0)
                text.fill = Color.BLUE

                rectangles.add(text)
            }

            val canvas = Canvas(colourRaster[0].size.toDouble() * squareSize + 1, colourRaster.size.toDouble() * squareSize + 1)
            val gc = canvas.graphicsContext2D

            canvas.layoutX = currentColumn * colourRaster[0].size.toDouble() * squareSize + currentColumn
            canvas.layoutY = currentRow * colourRaster.size.toDouble() * squareSize + currentRow

            gc.fill = Color.RED
            gc.fillRect(0.0,
                0.0,
                colourRaster[0].size.toDouble() * squareSize + 1,
                colourRaster.size.toDouble() * squareSize + 1)

            for (row in colourRaster.indices) {
                for (column in 0 until colourRaster[0].size) {
                    gc.fill = colourRaster[row][column]
                    gc.fillRect(column * squareSize.toDouble(),
                        row * squareSize.toDouble(),
                        squareSize.toDouble(),
                        squareSize.toDouble())
                }
            }
            rectangles.add(canvas)

            ++currentColumn
            if (currentColumn == rastersPerLine) {
                ++currentRow
                currentColumn = 0
            }

            ++rasterCounter
        }

        root.add(rectangles)

    }


}
