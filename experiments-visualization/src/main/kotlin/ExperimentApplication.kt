package com.kjipo

import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Matrix
import javafx.application.Application
import javafx.scene.paint.Color
import tornadofx.*
import java.util.*


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
