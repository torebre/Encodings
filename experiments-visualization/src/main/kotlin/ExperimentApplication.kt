package com.kjipo

import com.kjipo.experiments.MatrixVisualization
import com.kjipo.experiments.VisualizationData
import com.kjipo.representation.Matrix
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage
import java.util.*
import kotlin.math.min


class ExperimentApplication : Application() {
    private var root: StackPane? = null
    private var inputRasterData: InputRasterData? = null

    val logger = System.getLogger(ExperimentApplication::class.qualifiedName!!)

    override fun start(primaryStage: Stage?) {
        primaryStage?.let { stage ->
            root = StackPane().also {
                stage.scene = Scene(it, 300.0, 250.0)
            }

            stage.widthProperty().addListener { _, _, _ -> drawRasters() }
            stage.heightProperty().addListener { _, _, _ -> drawRasters() }
            stage.show()
        }

        instance = this
    }


    fun setInputRasterData(inputRasterData: InputRasterData) {
        this.inputRasterData = inputRasterData
        drawRasters()
    }


    private fun drawRasters() {
        inputRasterData?.let {
            drawRastersInternal(it, root?.height ?: 100.0, root?.width ?: 100.0)
        }
    }


    private fun drawRastersInternal(inputRasterData: InputRasterData, width: Double, height: Double) {
        root?.children?.clear()

        if (inputRasterData.inputRasters.isEmpty()) {
            return
        }

        val totalLengthAvailable = min(width, height)
        val numberOfRasters = inputRasterData.inputRasters.size
        val imageSize = inputRasterData.inputRasters.first().size

        val squareSize = totalLengthAvailable / imageSize * numberOfRasters

        var currentRow = 0
        var currentColumn = 0
        var rasterCounter = 0
        val rectangles = Group()

        inputRasterData.inputRasters.forEach { colourRaster ->

            if (inputRasterData.texts.isNotEmpty()) {
                val text = Text(
                    currentColumn.toDouble() * colourRaster[0].size * squareSize,
                    currentRow.toDouble() * colourRaster.size * squareSize,
                    inputRasterData.texts[rasterCounter]
                )
                text.font = Font(20.0)
                text.fill = Color.BLUE

                rectangles.children.add(text)
            }

            val kanjiImageCanvas =
                Canvas(
                    colourRaster[0].size.toDouble() * squareSize + 1,
                    colourRaster.size.toDouble() * squareSize + 1
                )
            val gc = kanjiImageCanvas.graphicsContext2D
            gc.fill = Color.RED
            gc.fillRect(
                0.0,
                0.0,
                colourRaster[0].size.toDouble() * squareSize + 1,
                colourRaster.size.toDouble() * squareSize + 1
            )

            kanjiImageCanvas.layoutX = currentColumn * colourRaster[0].size.toDouble() * squareSize + currentColumn
            kanjiImageCanvas.layoutY = currentRow * colourRaster.size.toDouble() * squareSize + currentRow

            for (row in colourRaster.indices) {
                for (column in 0 until colourRaster[0].size) {
                    gc.fill = colourRaster[row][column]
                    gc.fillRect(
                        column * squareSize,
                        row * squareSize,
                        squareSize,
                        squareSize
                    )
                }
            }
            rectangles.children.add(kanjiImageCanvas)

            ++currentColumn
            if (currentColumn == RASTERS_PER_LINE) {
                ++currentRow
                currentColumn = 0
            }
            ++rasterCounter
        }

        root?.children?.add(rectangles)
    }


    companion object {
        var instance: ExperimentApplication? = null
        private const val RASTERS_PER_LINE = 20


        fun displayVisualizationData(visualizationDataList: List<VisualizationData>) {
            val dataToDisplay = visualizationDataList.map { visualizationData ->
                val matrix = Matrix(
                    visualizationData.pointTypeImage.numberOfRows,
                    visualizationData.pointTypeImage.numberOfColumns
                ) { row, column ->
                    transformPointTypeToColour(*visualizationData.pointTypeImage[row, column])
                }
                matrix
            }

            showMatrixImages(dataToDisplay)
        }

        fun showMatrixImages(colourRasters: Collection<Matrix<Color>>) {
            showImages(colourRasters.map { matrix ->
                transformMatrixToColourArrays(matrix)
            })
        }

        fun <T> showMatrixVisualization(matrixVisualizations: Collection<MatrixVisualization<T>>, squareSize: Int = 1) {
            showMatrixImages(matrixVisualizations.map { createColorMatrix(it) })
        }

        fun showColourRasters(
            characters: MutableList<String>,
            colourRasters: Collection<Array<Array<Color>>>
        ) {
            val startThread = Thread {
                launch(ExperimentApplication::class.java)
            }
            startThread.start()

            var counter = 0
            do {
                if (counter > 10) {
                    throw RuntimeException("Application did not start in 10 seconds")
                }
                Thread.sleep(500)
                ++counter
            } while (instance == null)

            Platform.runLater {
                instance?.setInputRasterData(InputRasterData(characters, colourRasters))
            }
        }


        fun displayKanjiImage(kanjiImages: List<Matrix<Boolean>>, squareSize: Int = 1) {
            val kanjiImagesAsArrays = kanjiImages.map { kanjiImage ->
                Array(kanjiImage.numberOfRows) { row ->
                    Array(kanjiImage.numberOfColumns) { column ->
                        if (kanjiImage[row, column]) {
                            Color.WHITE
                        } else {
                            Color.BLACK
                        }
                    }
                }
            }

            showImages(kanjiImagesAsArrays)
        }


        private fun showImages(colourRasters: Collection<Array<Array<Color>>>) {
            showColourRasters(Collections.emptyList(), colourRasters)
        }

    }

    class InputRasterData(val texts: List<String> = emptyList(), val inputRasters: Collection<Array<Array<Color>>>)


}