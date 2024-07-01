package com.kjipo

import com.kjipo.experiments.MatrixVisualization
import com.kjipo.experiments.VisualizationData
import com.kjipo.representation.Matrix
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.util.*


class ExperimentApplication : Application() {
    private var root: StackPane? = null

    val logger = System.getLogger(ExperimentApplication::class.qualifiedName!!)

    override fun start(primaryStage: Stage?) {
        primaryStage?.let { stage ->
            root = StackPane().also {
                stage.scene = Scene(it, 300.0, 250.0)
            }

            stage.show()
        }

        instance = this
    }


    companion object {
        var instance: ExperimentApplication? = null


        fun displayVisualizationData(visualizationDataList: List<VisualizationData>, squareSize: Int = 1) {
            val dataToDisplay = visualizationDataList.map { visualizationData ->
                val matrix = Matrix(
                    visualizationData.pointTypeImage.numberOfRows,
                    visualizationData.pointTypeImage.numberOfColumns
                ) { row, column ->
                    transformPointTypeToColour(*visualizationData.pointTypeImage[row, column])
                }
                matrix
            }

            showMatrixImages(dataToDisplay, squareSize)
        }

        fun showMatrixImages(colourRasters: Collection<Matrix<Color>>, squareSize: Int = 1) {
            showImages(colourRasters.map { matrix ->
                transformMatrixToColourArrays(matrix)
            }, squareSize)
        }

        fun <T> showMatrixVisualization(matrixVisualization: MatrixVisualization<T>, squareSize: Int = 1) {
            showMatrixImages(Collections.singletonList(createColorMatrix(matrixVisualization)), squareSize)
        }

        fun <T> showMatrixVisualization(matrixVisualizations: Collection<MatrixVisualization<T>>, squareSize: Int = 1) {
            showMatrixImages(matrixVisualizations.map { createColorMatrix(it) }, squareSize)
        }

        fun showColourRasters(
            characters: MutableList<String>,
            squareSize: Int,
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
                instance?.root?.let {
                    ExperimentView.drawRasters(it.children, characters, squareSize, colourRasters)
                }
            }
        }


        fun displayKanjiImage(kanjiImage: Matrix<Boolean>, squareSize: Int = 1) {
            val kanjiImageAsArrays = Array(kanjiImage.numberOfRows) { row ->
                Array(kanjiImage.numberOfColumns) { column ->
                    if (kanjiImage[row, column]) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                }
            }

            showImages(Collections.singletonList(kanjiImageAsArrays), squareSize)
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

            showImages(kanjiImagesAsArrays, squareSize)
        }


        private fun showImages(colourRasters: Collection<Array<Array<Color>>>, squareSize: Int = 1) {
            showColourRasters(Collections.emptyList(), squareSize, colourRasters)
        }
    }


}