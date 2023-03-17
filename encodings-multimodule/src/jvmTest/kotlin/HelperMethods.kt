import com.kjipo.readetl.EtlDataReader
import com.kjipo.readetl.EtlDataSet
import com.kjipo.readetl.KanjiFromEtlData
import com.kjipo.representation.Matrix
import java.nio.file.Path



val colourMap = mapOf(
    Pair(1, IntArray(3).also { it[0] = 255; it[1] = 0; it[2] = 0 }),
    Pair(2, IntArray(3).also { it[0] = 0; it[1] = 255; it[2] = 0 })
)


internal fun loadImagesFromEtl9G(maxNumberOfFilesToRead: Int): List<KanjiFromEtlData> {
    return EtlDataReader.extractEtlImagesToKanjiData(
        Path.of("/home/student/Downloads/etlcbd_datasets"),
        EtlDataSet.ETL9G,
        maxNumberOfFilesToRead
    )
}

internal fun transformToBooleanMatrix(imageMatrix: Matrix<Int>): Matrix<Boolean> {
    return Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns, { _, _ -> false }).also { matrix ->
        imageMatrix.forEachIndexed { row, column, value -> matrix[row, column] = handlePixelValue(value) }
    }
}


internal fun handlePixelValue(rgbValue: Int): Boolean {
//        if (rgbValue != -1) {
//            println("RBG value: $rgbValue")
//        }

    if (rgbValue != -1) {
//                            image[row, column] = true
//                        }
//                        else {
        // https://stackoverflow.com/questions/49676862/srgb-to-rgb-color-conversion
        val blue: Int = rgbValue and 255
        val green: Int = rgbValue shr 8 and 255
        val red: Int = rgbValue shr 16 and 255

        // https://www.baeldung.com/cs/convert-rgb-to-grayscale
        val grayscale = (0.3 * red) / 255 + (0.59 * green) / 255 + (0.11 * blue) / 255

        if (blue < 255 || green < 254 || red < 255) {
            println("Test30: $red, $green, $blue")
            println("Grayscale: $grayscale")
        }

        if (grayscale > 0.9) {
            return true
        }
    }
    return false

}
