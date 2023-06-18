import com.kjipo.readetl.KanjiFromEtlData
import com.kjipo.representation.Matrix
import com.kjipo.representation.pointsmatching.Direction
import com.kjipo.representation.pointsmatching.PointsPlacer
import com.kjipo.representation.raster.makeSquare
import java.io.File

class PointsTest {

    fun runRegionExtraction() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, PointsTest::simpleThreshold))

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
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, PointsTest::simpleThreshold))

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
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, PointsTest::simpleThreshold))
        val pointsPlacer = PointsPlacer(imageMatrix)

        writeOutputMatrixToPngFile(
            pointsPlacer.linePointMatrix,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_line_points.png")
        ) { value -> colourFunction(value) }
    }


    fun runFindCenterMass() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, PointsTest::simpleThreshold))
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


    /**
     * Mark the borders of each region and write the result as an
     * image to a file.
     */
    fun runBorderExtraction() {
        val dataset = loadImagesFromEtl9G(1).first()
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, PointsTest::simpleThreshold))
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
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, PointsTest::simpleThreshold))
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
        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData, PointsTest::simpleThreshold))
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
//    pointsTest.runFindCenterMass()
//    pointsTest.runBorderExtraction()
//    pointsTest.runBorderExtractionShowOneBorderRegion()
    pointsTest.runBorderEncodingOnOneRegion()

}
