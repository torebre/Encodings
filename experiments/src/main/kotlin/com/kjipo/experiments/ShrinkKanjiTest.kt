package com.kjipo.experiments

import com.kjipo.segmentation.shrinkImage
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.displayMatrix
import com.kjipo.visualization.loadEncodedKanji
import java.nio.file.Paths


fun shrinkKanjitest() {
    val encodedKanji = loadEncodedKanji(Paths.get("kanji_output8/26681.dat"))
    val image = transformArraysToMatrix(encodedKanji.image)

    val shrinkImage = shrinkImage(image, 32, 32)


    displayMatrix(shrinkImage, 20)
}



fun main(args: Array<String>) {
    shrinkKanjitest()

}