package com.kjipo

import com.kjipo.readetl.EtlDataReader.flipMatrix
import com.kjipo.readetl.EtlDataReader.readImage
import com.kjipo.readetl.KanjiFromEtlData
import kotlin.io.path.Path


class TestSet(val target: KanjiSetIdentifier, val testSet: List<KanjiSetIdentifier>) {

    fun getImageDataForTarget(datasetRoot: String): KanjiFromEtlData {
        return getKanjiImageData(target, datasetRoot)
    }

    fun getImageDataForTestImage(testDataItemNumber: Int, datasetRoot: String): KanjiFromEtlData {
        return getKanjiImageData(testSet[testDataItemNumber], datasetRoot)
    }

    fun getTestSetSize(): Int {
        return testSet.size
    }

    companion object {

        private fun getKanjiImageData(kanjiSetIdentifier: KanjiSetIdentifier, datasetRoot: String): KanjiFromEtlData {
            val directoryName = buildString {
                append("0x")
                append(kanjiSetIdentifier.unicode.toString(16))
            }
            val kanjiImageFile = Path(datasetRoot).resolve(kanjiSetIdentifier.etlDataSet.name)
                .resolve(directoryName)
                .resolve(kanjiSetIdentifier.fileName)

            return KanjiFromEtlData(
                kanjiSetIdentifier.unicode,
                kanjiSetIdentifier.etlDataSet,
                kanjiImageFile,
                flipMatrix(readImage(kanjiImageFile))
            )
        }

    }

}