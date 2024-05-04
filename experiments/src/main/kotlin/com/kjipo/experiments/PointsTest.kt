package com.kjipo.experiments

import com.kjipo.matching.EndpointFeature
import com.kjipo.matching.EndpointTriplet
import com.kjipo.readetl.EtlDataReader.extractEtlImagesForUnicodeToKanjiData
import com.kjipo.readetl.KanjiFromEtlData
import com.kjipo.representation.Matrix
import com.kjipo.representation.pointsmatching.Direction
import com.kjipo.representation.pointsmatching.EndPointMatchData
import com.kjipo.representation.pointsmatching.PointsPlacer
import com.kjipo.representation.raster.bwmorphEndpoints
import com.kjipo.representation.raster.makeSquare
import com.kjipo.representation.raster.makeThin
import com.kjipo.representation.raster.scaleMatrix
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class PointsTest {


    fun runRegionExtraction() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, Companion::simpleThreshold))

        val pointsPlacer = PointsPlacer(imageMatrix)

        pointsPlacer.runPlacement()

        val result = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { _, _ -> 0 }

        for (row in 0 until imageMatrix.numberOfRows) {
            for (column in 0 until imageMatrix.numberOfColumns) {
                if (imageMatrix[row, column]) {
                    result[row, column] = pointsPlacer.regionMatrix[row, column] + 10
//                    result[row, column] = 2
                }
            }
        }

        pointsPlacer.getPoints().forEach { point ->
            result[point.first, point.second] = 1
        }

        writeOutputMatrixToPngFile(
            result,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_points.png")
        ) { value -> colourFunction(value) }
    }


    fun extractBorders(dataset: KanjiFromEtlData = loadImagesFromEtl9G(1).first()) {
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, Companion::simpleThreshold))

        val pointsPlacer = PointsPlacer(imageMatrix)

        pointsPlacer.runPlacement()

        val result = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { _, _ -> 0 }

        for (row in 0 until imageMatrix.numberOfRows) {
            for (column in 0 until imageMatrix.numberOfColumns) {
                if (imageMatrix[row, column]) {
                    result[row, column] = pointsPlacer.regionMatrix[row, column] + 10
//                    result[row, column] = 2
                }
            }
        }

        pointsPlacer.getPoints().forEach { point ->
            result[point.first, point.second] = 1
        }


    }


    fun runFindLinePoints() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, Companion::simpleThreshold))
        val pointsPlacer = PointsPlacer(imageMatrix)

        writeOutputMatrixToPngFile(
            pointsPlacer.linePointMatrix,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_line_points.png")
        ) { value -> colourFunction(value) }
    }


    fun runFindCenterMass() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, Companion::simpleThreshold))
        val pointsPlacer = PointsPlacer(imageMatrix)

        val result = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns)
        { row, column -> pointsPlacer.regionMatrix[row, column] }

        for (row in 0 until imageMatrix.numberOfRows) {
            for (column in 0 until imageMatrix.numberOfColumns) {
                if (pointsPlacer.centerOfMassMatrix[row, column] != PointsPlacer.backgroundRegion) {
                    result[row, column] = 1
                }
            }
        }

        pointsPlacer.getPoints().forEach { point ->
            result[point.first, point.second] = 1
        }

        writeOutputMatrixToPngFile(
            result,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_center_mass_points.png")
        ) { value ->
            if (colourMap.containsKey(value)) {
                colourMap[value]
            } else {
                colourFunction(value)
            }
        }
    }


    private fun findCenterMassForImage(imageMatrix: Matrix<Boolean>): Matrix<Int> {
        val pointsPlacer = PointsPlacer(imageMatrix)

        val result = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns)
        { row, column -> pointsPlacer.regionMatrix[row, column] }

        for (row in 0 until imageMatrix.numberOfRows) {
            for (column in 0 until imageMatrix.numberOfColumns) {
                if (pointsPlacer.centerOfMassMatrix[row, column] != PointsPlacer.backgroundRegion) {
                    result[row, column] = 1
                }
            }
        }

        pointsPlacer.getPoints().forEach { point ->
            result[point.first, point.second] = 1
        }

        return result
    }


    fun createImagesForUnicode() {
        val unicode = 32769
        val outputDirectory = Path.of("/home/student/workspace/testEncodings/temp/kanjiset/")
        val imageMatrix =
            loadKanjiMatrix(outputDirectory.resolve("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat"))

        if (!outputDirectory.exists()) {
            outputDirectory.createDirectory()
        }

        writeOutputMatrixToPngFile(
            imageMatrix,
            outputDirectory.resolve("${unicode}_kanji_from_input_file.png").toFile()
        ) { value ->
            if (value) colourMap[0] else colourMap[1]
        }

        extractEtlImagesForUnicodeToKanjiData(unicode).forEach {
            val tranformedEtlKanjiData = makeSquare(transformToBooleanMatrix(it.kanjiData, Companion::simpleThreshold))
            writeOutputMatrixToPngFile(
                tranformedEtlKanjiData,
                outputDirectory.resolve("example_${it.getFileNameWithoutSuffix()}.png").toFile()
            ) { value ->
                if (value) colourMap[0] else colourMap[1]
            }
        }
    }

    fun createMassCenterForImages() {
        val unicode = 32769
        val outputDirectory = Path.of("/home/student/workspace/testEncodings/temp/centermass/")

        if (!outputDirectory.exists()) {
            outputDirectory.createDirectory()
        }

        val imageMatrix =
            loadKanjiMatrix(outputDirectory.resolve("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat"))

        writeOutputMatrixToPngFile(
            imageMatrix,
            outputDirectory.resolve("${unicode}_kanji_from_input_file.png").toFile()
        ) { value ->
            if (value) colourMap[0] else colourMap[1]
        }

        extractEtlImagesForUnicodeToKanjiData(unicode).map {
            Pair(it.filePath, makeSquare(transformToBooleanMatrix(it.kanjiData, Companion::simpleThreshold)))
        }
            .map {
                Pair(it.first, PointsPlacer(it.second))
            }
            .forEach {
                val regions = mutableSetOf<Int>()
                it.second.regionMatrix.forEach { value ->
                    regions.add(value)
                }
//                logger.info { "Number of regions: ${regions.size}" }

                val numberOfColoursNeeded = regions.size + PointsPlacer.startRegionCount + 1
                val centerOfMassPoints = it.second.getCenterOfMassPoints().map { it.coordinates }.toSet()

                writeOutputMatrixToPngFile(
                    it.second.regionMatrix,
                    outputDirectory.resolve("example_${it.first.nameWithoutExtension}_center_mass.png").toFile()
                ) { row, column, value ->
                    if (centerOfMassPoints.contains(Pair(row, column))) {
                        IntArray(3).also {
                            it[0] = 0
                            it[1] = 255
                            it[2] = 255
                        }
                    } else {
                        colourFunction(value, numberOfColoursNeeded)
//                    if (value) colourMap[0] else colourMap[1]
                    }
                }
            }
    }

    private fun createMassCenterForImage(fileNameWithoutExtension: String, imageData: Matrix<Int>) {
        val outputDirectory = Path.of("/home/student/workspace/testEncodings/temp/centermass/")
        if (!outputDirectory.exists()) {
            outputDirectory.createDirectory()
        }

        writeOutputMatrixToPngFile(
            imageData,
            outputDirectory.resolve("${fileNameWithoutExtension}_center_mass_points.png").toFile()
        ) { value ->
            if (value > 0) colourMap[0] else colourMap[1]
        }

    }

    fun runExtraction() {
        val unicode = 32769
        val outputDirectory = Path.of("/home/student/workspace/testEncodings/temp/placement/")

        if (!outputDirectory.exists()) {
            outputDirectory.createDirectory()
        }

        val imageMatrix =
            makeThin(makeSquare(loadKanjiMatrix(Path.of("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat"))))

        writeOutputMatrixToPngFile(
            imageMatrix,
            outputDirectory.resolve("${unicode}_kanji_from_input_file.png").toFile()
        ) { value ->
            if (value) colourMap[0] else colourMap[1]
        }

        extractEtlImagesForUnicodeToKanjiData(unicode).map {
            Pair(it.filePath, makeThin(makeSquare(transformToBooleanMatrix(it.kanjiData, Companion::simpleThreshold))))
        }
            .forEach {
                val pointsPlacer = PointsPlacer(it.second)
                val endPoints = mutableSetOf<Pair<Int, Int>>()

                bwmorphEndpoints(it.second).forEachIndexed { row, column, value ->
                    if (value) {
                        endPoints.add(Pair(row, column))
                    }
                }

                val regions = mutableSetOf<Int>()
                pointsPlacer.regionMatrix.forEach { value ->
                    regions.add(value)
                }
//                logger.info { "Number of regions: ${regions.size}" }

                writeOutputMatrixToPngFile(
                    it.second,
                    outputDirectory.resolve("example_${it.first.nameWithoutExtension}_extraction.png").toFile()
                ) { row, column, value ->
                    if (endPoints.contains(Pair(row, column))) {
                        IntArray(3).also {
                            it[0] = 0
                            it[1] = 255
                            it[2] = 255
                        }
                    } else {
                        if (value) colourMap[0] else colourMap[1]
                    }
                }
            }
    }


    fun setupEndpointMatching(): List<VisualizationData> {
        val unicode = 32769
        val inputSquareMatrix =
            transposeMatrix(makeSquare(loadKanjiMatrix(Path.of("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat"))))
        val imageMatrix = makeThin(inputSquareMatrix)


        // TODO Remove restriction on the number of images to load
        return extractEtlImagesForUnicodeToKanjiData(unicode, 5).map {
            val squareMatrix = transposeMatrix(
                makeSquare(
                    scaleMatrix(
                        transformToBooleanMatrix(
                            it.kanjiData,
                            Companion::simpleThreshold
                        ), 128, 128
                    )
                )
            )

            val thinnedImage = makeThin(squareMatrix)
            val endPoints = mutableSetOf<Pair<Int, Int>>()

            bwmorphEndpoints(thinnedImage).forEachIndexed { row, column, value ->
                if (value) {
                    endPoints.add(Pair(row, column))
                }
            }

            val relationalDataForImage = setupMatchData(endPoints)
            extractVisualizationData(squareMatrix, relationalDataForImage)
        }.toList()

    }

    private fun extractVisualizationData(
        imageMatrix: Matrix<Boolean>,
        relationDataImage: RelationDataForImage
    ): VisualizationData {
        val pointSpecifications =
            Matrix<Array<PointType>>(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { row, column ->
                if (imageMatrix[row, column]) {
                    arrayOf(PointType.LINE)
                } else {
                    arrayOf(PointType.EMPTY)
                }
            }

        relationDataImage.endpointsRelationData.forEach { endpointFeature ->
            pointSpecifications[endpointFeature.location.first, endpointFeature.location.second] += arrayOf(PointType.ENDPOINT)
        }

        return VisualizationData(imageMatrix, pointSpecifications)
    }


    private fun matchEndpointTriplets(
        endpointTriplet: EndpointTriplet,
        endpointTripletsToMatch: List<EndpointTriplet>
    ): List<EndpointTripletDistance> {
        val endpointTripletDistances = endpointTripletsToMatch.map { endpointTripletToMatch ->
            val distance =
                (endpointTriplet.relativeDistance - endpointTripletToMatch.relativeDistance).absoluteValue + (endpointTriplet.dotProduct - endpointTripletToMatch.dotProduct).absoluteValue
            EndpointTripletDistance(endpointTriplet, endpointTripletToMatch, distance)
        }.toList()

        return endpointTripletDistances.sortedBy { it.distance }
    }

    fun setupMatchData(endpoints: Set<Pair<Int, Int>>): RelationDataForImage {
        var idCounter = 0
        val endPointFeatures = endpoints.map {
            EndpointFeature(idCounter++, it)
        }

        val relationData = mutableMapOf<Pair<Int, Int>, EndpointsRelationData>()
        var counter = 1

        for (endPointFeature in endPointFeatures) {
            for (i in counter until endPointFeatures.size) {
                relationData[Pair(endPointFeature.id, endPointFeatures[i].id)] =
                    createEndpointRelationData(endPointFeature, endPointFeatures[i])
            }
            ++counter
        }

        return RelationDataForImage(endPointFeatures, relationData)
    }

    private fun createEndpointRelationData(
        endpointFeature1: EndpointFeature,
        endpointFeature2: EndpointFeature
    ): EndpointsRelationData {
        return EndpointsRelationData(
            endpointFeature1,
            endpointFeature2,
            computeDistance(endpointFeature1, endpointFeature2)
        )
    }

    private fun computeDistance(
        endpointFeature1: EndpointFeature,
        endpointFeature2: EndpointFeature
    ): Double {
        return sqrt(
            (endpointFeature1.location.first - endpointFeature2.location.first).toDouble().pow(2) +
                    (endpointFeature1.location.second - endpointFeature2.location.second).toDouble().pow(2)
        )
    }


    fun runMatch() {
        val unicode = 32769

//        val imageMatrix =
//            makeThin(makeSquare(loadKanjiMatrix(Path.of("/home/student/workspace/testEncodings/temp/kanjiOutput/$unicode.dat"))))

        val endPointMatchData = extractEtlImagesForUnicodeToKanjiData(unicode, 5).map {
            Pair(it.filePath, makeThin(makeSquare(transformToBooleanMatrix(it.kanjiData, Companion::simpleThreshold))))
        }.map {
            val endPoints = mutableSetOf<Pair<Int, Int>>()

            bwmorphEndpoints(it.second).forEachIndexed { row, column, value ->
                if (value) {
                    endPoints.add(Pair(row, column))
                }
            }

            EndPointMatchData(it.second, endPoints)
        }

        for (data1 in endPointMatchData) {
            for (data2 in endPointMatchData) {
                EndPointMatchData.match(data1, data2)

            }

        }


    }


    /**
     * Mark the borders of each region and write the result as an
     * image to a file.
     */
    fun runBorderExtraction() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, Companion::simpleThreshold))
        val pointsPlacer = PointsPlacer(imageMatrix)
        val result = pointsPlacer.extractBorders()
        val borderMatrix = Matrix(
            imageMatrix.numberOfRows,
            imageMatrix.numberOfColumns
        ) { _, _ -> PointsPlacer.backgroundRegion }

        result.forEachIndexed { index, value ->
            value.points.forEach {
                borderMatrix[it.first, it.second] = index + PointsPlacer.startRegionCount
            }
        }

        writeOutputMatrixToPngFile(
            borderMatrix,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_border_extraction.png")
        ) { value -> colorMapFallbackFunction(value) }

    }


    /**
     * Extract the borders and write an image showing a single border
     * region to a file.
     */
    fun runBorderExtractionShowOneBorderRegion() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, Companion::simpleThreshold))
        val pointsPlacer = PointsPlacer(imageMatrix)
        val result = pointsPlacer.extractBorderStructure()
        val borderMatrix = Matrix(
            imageMatrix.numberOfRows,
            imageMatrix.numberOfColumns
        ) { _, _ -> PointsPlacer.backgroundRegion }

        result.borders[1].points.forEach { point ->
            borderMatrix[point.first, point.second] = PointsPlacer.startRegionCount
        }

        writeOutputMatrixToPngFile(
            borderMatrix,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_border_extraction_single_region.png")
        ) { value -> colorMapFallbackFunction(value) }

    }


    fun runBorderEncodingOnOneRegion() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, Companion::simpleThreshold))
        val pointsPlacer = PointsPlacer(imageMatrix)
        val allBordersMatrix = pointsPlacer.extractBorderStructure()
        val borderMatrix = Matrix(
            imageMatrix.numberOfRows,
            imageMatrix.numberOfColumns
        ) { _, _ -> PointsPlacer.backgroundRegion }

        allBordersMatrix.borders[1].points.forEach { point ->
            borderMatrix[point.first, point.second] = PointsPlacer.startRegionCount
        }

        val encodedBorder = pointsPlacer.encodeBorderIntoDirectionList(borderMatrix)
        val result = Matrix<Direction?>(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { _, _ ->
            null
        }

        encodedBorder.forEach { result[it.row, it.column] = it.direction }

        writeOutputMatrixToPngFile(
            result,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_border_encoding_single_region.png")
        ) { value -> colourFunction(value) }
    }


    companion object {

        private fun simpleThreshold(rgbValue: Int): Boolean {
            if (rgbValue != -1) {
                // https://stackoverflow.com/questions/49676862/srgb-to-rgb-color-conversion
                val blue: Int = rgbValue and 255
                val green: Int = rgbValue shr 8 and 255
                val red: Int = rgbValue shr 16 and 255

                return red > 20 || green > 20 || blue > 20
            }
            return false
        }
    }

}


fun main() {
    val pointsTest = PointsTest()
//    pointsTest.runRegionExtraction()
//    pointsTest.runFindLinePoints()
//    pointsTest.runBorderExtraction()
//    pointsTest.runBorderExtractionShowOneBorderRegion()
//    pointsTest.runBorderEncodingOnOneRegion()

//    pointsTest.runFindCenterMass()
//    pointsTest.createImagesForUnicode()
//    pointsTest.createMassCenterForImages()

//    pointsTest.runExtraction()

    pointsTest.setupEndpointMatching()

}
