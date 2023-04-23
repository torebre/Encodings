import com.kjipo.representation.Matrix
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
                    result[row, column] = 2
                }
            }
        }

        pointsPlacer.getPoints().forEach { point ->
            result[point.first, point.second] = 1
        }

        writeOutputMatrixToPngFile(
            result,
            File("${dataset.filePath.fileName.toString().substringBefore('.')}_points.png"),
            colourMap
        )


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
    pointsTest.runRegionExtraction()
}
