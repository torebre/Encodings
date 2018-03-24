package com.kjipo.visualization

import javafx.collections.ObservableList
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*


class KanjiView : View("Kanji overview") {
    var rasters: Collection<Array<Array<Color>>> = emptyList()
    val group: Group = Group()

    val rastersPerLine = 20
    val squareSize = 1


    fun loadRasters(colourRasters: Collection<Array<Array<Color>>>, texts: List<String> = emptyList()) {
        this.rasters = colourRasters
        group.children.clear()
        drawRasters(group.children, texts)
    }

    override val root = scrollpane {
        content = group
        drawRasters(group.children)
    }

    fun drawRasters(root: ObservableList<Node>, texts: List<String> = emptyList()) {
        if (rasters.isEmpty()) {
            return
        }

        var currentRow = 0
        var currentColumn = 0
        var rasterCounter = 0
        val rectangles = Group()

        val firstRaster = rasters.iterator().next()

        rasters.forEach {

            if(texts.isNotEmpty()) {
                val text = Text(currentColumn.toDouble() * firstRaster[0].size * squareSize, currentRow.toDouble() * firstRaster.size * squareSize, texts[rasterCounter])
                text.font = Font(20.0)
                text.fill = Color.BLUE

                rectangles.add(text)
            }

            for (row in it.indices) {
                for (column in 0 until it[0].size) {
                    val rectangle = javafx.scene.shape.Rectangle(
                            (column * squareSize).toDouble() + currentColumn * firstRaster[0].size * squareSize,
                            (row * squareSize).toDouble() + currentRow * firstRaster.size * squareSize,
                            squareSize.toDouble(),
                            squareSize.toDouble())

                    rectangle.fill = it[row][column]
                    rectangles.add(rectangle)

                    rectangles.add(rectangle)
                }
            }

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