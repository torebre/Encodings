package com.kjipo

import com.kjipo.experiments.PointType
import com.kjipo.experiments.VisualizationData
import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Matrix
import javafx.application.Application
import javafx.scene.paint.Color
import tornadofx.*
import java.util.*


val logger = System.getLogger(ExperimentApplication::class.qualifiedName!!)

class ExperimentApplication : App() {
    override val primaryView = ExperimentView::class
}


fun displayKanjis(encodedKanjis: Collection<EncodedKanji>, squareSize: Int = 1) {
    val startThread = Thread {
        Application.launch(ExperimentApplication::class.java)
    }
    startThread.start()

    val characters = mutableListOf<String>()
    val colourRasters = encodedKanjis.map {
        characters.add(String(Character.toChars(it.unicode)))

        Array(it.image.size) { row ->
            Array(it.image[0].size) { column ->
                if (it.image[row][column]) {
                    Color.WHITE
                } else {
                    Color.BLACK
                }
            }
        }
    }

    val experimentView = FX.find(ExperimentView::class.java)
    FX.runAndWait { experimentView.loadRasters(colourRasters, characters, squareSize) }
}

fun displayKanjiImage(kanjiImage: Matrix<Boolean>, squareSize: Int = 1) {
    val startThread = Thread {
        Application.launch(ExperimentApplication::class.java)
    }
    startThread.start()

    val kanjiImageAsArrays = Array(kanjiImage.numberOfRows) { row ->
        Array(kanjiImage.numberOfColumns) { column ->
            if (kanjiImage[row, column]) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        }
    }

    val experimentView = FX.find(ExperimentView::class.java)
    FX.runAndWait {
        experimentView.loadRasters(
            Collections.singletonList(kanjiImageAsArrays),
            Collections.emptyList(),
            squareSize
        )
    }
}

fun displayVisualizationData(visualizationDataList: List<VisualizationData>, squareSize: Int = 1) {
    val dataToDisplay = visualizationDataList.map { visualizationData ->
        val matrix = Matrix(
            visualizationData.pointTypeImage.numberOfRows,
            visualizationData.pointTypeImage.numberOfColumns
        ) { row, column ->
            transformPointTypeToColour(*visualizationData.pointTypeImage[row, column])
        }

//        println("Number of rows: ${matrix.numberOfRows}. Number of columns: ${matrix.numberOfColumns}")

        matrix
    }

    showMatrixImages(dataToDisplay, squareSize)
}

private fun transformPointTypeToColour(vararg pointType: PointType): Color {
    // TODO Should have better handling of multiple point types
    return pointType.map {
        when (it) {
            PointType.EMPTY -> Color.color(0.0, 0.0, 0.0)
            PointType.ENDPOINT -> Color.color(1.0, 0.0, 0.0)
            PointType.LINE -> Color.color(1.0, 1.0, 1.0)
        }
    }.last()
}


fun displayKanjiImage(kanjiImages: List<Matrix<Boolean>>, squareSize: Int = 1) {
    val startThread = Thread {
        Application.launch(ExperimentApplication::class.java)
    }
    startThread.start()

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

    val experimentView = FX.find(ExperimentView::class.java)
    FX.runAndWait {
        experimentView.loadRasters(
            kanjiImagesAsArrays,
            Collections.emptyList(),
            squareSize
        )
    }
}


private fun showMatrixImages(colourRasters: Collection<Matrix<Color>>, squareSize: Int = 1) {
    showImages(colourRasters.map { matrix ->

        logger.log(System.Logger.Level.INFO, "Matrix: " +matrix.numberOfRows +", " +matrix.numberOfColumns)

        Array(matrix.numberOfRows) { row ->
            Array(matrix.numberOfColumns) { column ->
                matrix[row, column]
            }
        }
    }, squareSize)
}

private fun showImages(colourRasters: Collection<Array<Array<Color>>>, squareSize: Int = 1) {
    val startThread = Thread {
        Application.launch(ExperimentApplication::class.java)
    }
    startThread.start()

    val experimentView = FX.find(ExperimentView::class.java)
    FX.runAndWait {
        experimentView.loadRasters(
            colourRasters,
            Collections.emptyList(),
            squareSize
        )
    }
}