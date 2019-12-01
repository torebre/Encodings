package com.kjipo.representation.linefitting

//import com.kjipo.representation.EncodedKanji
//import com.kjipo.representation.line.AngleLine
//import com.kjipo.representation.raster.makeThin
//import com.kjipo.representation.segmentation.shrinkImage
//
//
//fun fitLinesToKanji(encodedKanji: EncodedKanji, minimumLengthToInclude: Int = 1): List<AngleLine> {
//    val kanjiMatrix = _root_ide_package_.com.kjipo.representation.raster.transformArraysToMatrix(encodedKanji.image)
//    val standardizedImage = makeThin(shrinkImage(kanjiMatrix, 64, 64))
//    val linePrototypes = fitMultipleLinesUsingDevianceMeasure(standardizedImage).filter { it.length >= minimumLengthToInclude }
//    return linePrototypes
//}