package com.kjipo.experiments

import com.kjipo.segmentation.shrinkImage
import com.kjipo.setup.transformKanjiData
import com.kjipo.skeleton.transformArraysToMatrix
import com.kjipo.visualization.loadKanjisFromDirectory
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption



private object CreateDataset {

    fun createDataset() {
        val loadedKanji = loadKanjisFromDirectory(Paths.get("kanji_output8"))
        val outputDirectory = Paths.get("fragments2")

        if (!Files.exists(outputDirectory)) {
            Files.createDirectory(outputDirectory)
        }

        loadedKanji.map {
            val image = transformArraysToMatrix(it.image)
            Pair(it.unicode, shrinkImage(image, 32, 32))
        }
                .forEach { pair ->
                    Files.newBufferedWriter(outputDirectory.resolve(pair.first.toString().plus(".dat")),
                            StandardCharsets.UTF_8, StandardOpenOption.CREATE).use {
                        it.write(transformKanjiData(pair.second))
                    }
                }
    }


    @JvmStatic
    fun main(args: Array<String>) {
        createDataset()

    }


}