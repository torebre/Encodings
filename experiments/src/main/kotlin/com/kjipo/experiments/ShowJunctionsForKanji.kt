package com.kjipo.experiments

import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.extractJunctions
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.displayColourMatrix
import com.kjipo.visualization.loadEncodedKanji
import javafx.scene.paint.Color
import java.nio.file.Paths


fun showJunctionsForKanji() {
//    val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/26681.dat"))
    val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/33550.dat"))
    val image = transformArraysToMatrix(encodedKanji.image)

    val shrinkImage = shrinkImage(image, 32, 32)
    val junctions = extractJunctions(shrinkImage)


    val dispImage = Matrix(shrinkImage.numberOfRows, shrinkImage.numberOfColumns, { row, column ->
        if (shrinkImage[row, column]) {
            Color.WHITE
        } else {
            Color.BLACK
        }
    })

    junctions.forEachIndexed { row, column, value ->
        if (value) {
            dispImage[row, column] = Color.RED
        }
    }


//    displayMatrix(junctions, 20)


    displayColourMatrix(dispImage, 20)


}


fun main(args: Array<String>) {
    showJunctionsForKanji()
}