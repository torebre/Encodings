import com.kjipo.readetl.EtlDataReader
import com.kjipo.readetl.EtlDataSet
import com.kjipo.readetl.KanjiFromEtlData
import com.kjipo.representation.Matrix
import com.kjipo.representation.pointsmatching.Direction
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.max


val colourMap = mapOf(
    Pair(0, IntArray(3).also { it[0] = 0; it[1] = 0; it[2] = 0 }),
    Pair(1, IntArray(3).also { it[0] = 255; it[1] = 0; it[2] = 0 }),
    Pair(2, IntArray(3).also { it[0] = 0; it[1] = 255; it[2] = 0 })
)

fun colorMapFallbackFunction(value: Int): IntArray {
    return if (colourMap.containsKey(value)) {
        colourMap[value]!!
    } else {
        colourFunction(value)
    }
}


fun colourFunction(value: Direction?): IntArray {
    if (value == null) {
        return IntArray(3).also { it[0] = 0; it[1] = 0; it[2] = 0 }
    }
//    println(value.ordinal.toFloat().div(Direction.values().size))

    return Color.getHSBColor(value.ordinal.toFloat().div(Direction.values().size), 1.0f, 1.0f).let { colour ->
        IntArray(3).also {
            it[0] = colour.red
            it[1] = colour.green
            it[2] = colour.blue
        }
    }

}

fun colourFunction(value: Int, maxValue: Int = 50): IntArray {
    if (value < 0 || value > maxValue) {
        throw IllegalArgumentException("Value needs to be in range from 0 to $maxValue (inclusive): $value")
    }

    return Color.getHSBColor(value.toFloat().div(maxValue), 1.0f, 1.0f).let { colour ->
        IntArray(3).also {
            it[0] = colour.red
            it[1] = colour.green
            it[2] = colour.blue
        }
    }

}

fun colourFunction(value: Double): IntArray {
    return Color.getHSBColor(value.toFloat(), 1.0f, 1.0f).let { colour ->
        IntArray(3).also {
            it[0] = colour.red
            it[1] = colour.green
            it[2] = colour.blue
        }
    }

}

internal fun loadImagesFromEtl9G(maxNumberOfFilesToRead: Int): List<KanjiFromEtlData> {
    return EtlDataReader.extractEtlImagesToKanjiData(
        Path.of("/home/student/Downloads/etlcbd_datasets"),
        EtlDataSet.ETL9G,
        maxNumberOfFilesToRead
    )
}

internal fun transformToBooleanMatrix(
    imageMatrix: Matrix<Int>,
    thresholdFunction: (rgbValue: Int) -> Boolean = { value -> handlePixelValue(value) }
): Matrix<Boolean> {
    return Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns, { _, _ -> false }).also { matrix ->
        imageMatrix.forEachIndexed { row, column, value -> matrix[row, column] = thresholdFunction(value) }
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

//        if (blue < 255 || green < 254 || red < 255) {
//            println("Test30: $red, $green, $blue")
//            println("Grayscale: $grayscale")
//        }

        if (grayscale > 0.9) {
            return true
        }
    }
    return false

}

internal fun writeOutputMatrixToPngFile(result: Matrix<Int>, outputFile: File, colourMap: Map<Int, IntArray>) {
    return writeOutputMatrixToPngFile(result, outputFile) { value ->
        colourMap[value]!!
    }
}


internal fun <T> writeOutputMatrixToPngFile(
    result: Matrix<T>,
    outputFile: File,
    colourProvider: (T) -> IntArray?
) {
    val bufferedImage = BufferedImage(
        result.numberOfRows,
        result.numberOfColumns,
        BufferedImage.TYPE_INT_RGB
    )

    result.forEachIndexed { row, column, value ->
        colourProvider(value).let {
            bufferedImage.raster.setPixel(row, column, it)
        }
    }

    ImageIO.write(bufferedImage, "png", outputFile)
}
