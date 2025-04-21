package com.kjipo.experiments

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


/**
 * This method is AI-generated
 *
 */
fun generateEvenlyDistributedColors(count: Int): List<PointColor> {
    if (count <= 0) {
        return emptyList()
    }

    // Use golden ratio to help spread the hues evenly
    val goldenRatio = 0.618033988749895

    return List(count) { i ->
        // Use golden ratio to generate hue
        val hue = (i * goldenRatio) % 1.0
        // Keep saturation and value at maximum for most distinct colors
        val saturation = 0.95
        val value = 0.95

        // Convert HSV to RGB
        val h = hue * 6.0
        val c = value * saturation
        val x = c * (1 - Math.abs((h % 2) - 1))
        val m = value - c

        val (r, g, b) = when (h.toInt()) {
            0 -> Triple(c, x, 0.0)
            1 -> Triple(x, c, 0.0)
            2 -> Triple(0.0, c, x)
            3 -> Triple(0.0, x, c)
            4 -> Triple(x, 0.0, c)
            5 -> Triple(c, 0.0, x)
            else -> Triple(c, x, 0.0)
        }

        PointColor(
            (r + m).coerceIn(0.0, 1.0),
            (g + m).coerceIn(0.0, 1.0),
            (b + m).coerceIn(0.0, 1.0)
        )
    }
}


fun generateEvenlyDistributedColors2(count: Int): List<PointColor> {
    if (count <= 0) {
        return emptyList()
    }

    val colorStep = 360.0 / count
    return List(count) { i ->
        val hue = i * colorStep

        val saturation = 0.95
        val value = 0.95

        hsvToRgb(hue, saturation, value)
    }
}

/**
 * This method is AI-generated
 *
 * Converts HSV color values to RGB color.
 * @param hue Hue value in range [0, 360] degrees
 * @param saturation Saturation value in range [0, 1]
 * @param value Value/Brightness in range [0, 1]
 * @return PointColor containing RGB values in range [0, 1]
 */
fun hsvToRgb(hue: Double, saturation: Double, value: Double): PointColor {
    // Ensure hue is in [0, 360] range
    val h = (hue % 360 + 360) % 360
    // Clamp saturation and value to [0, 1]
    val s = saturation.coerceIn(0.0, 1.0)
    val v = value.coerceIn(0.0, 1.0)

    val c = v * s // Chroma
    val x = c * (1 - Math.abs((h / 60.0) % 2 - 1))
    val m = v - c

    val (r1, g1, b1) = when {
        h < 60 -> Triple(c, x, 0.0)
        h < 120 -> Triple(x, c, 0.0)
        h < 180 -> Triple(0.0, c, x)
        h < 240 -> Triple(0.0, x, c)
        h < 300 -> Triple(x, 0.0, c)
        else -> Triple(c, 0.0, x)
    }

    return PointColor(
        (r1 + m).coerceIn(0.0, 1.0),
        (g1 + m).coerceIn(0.0, 1.0),
        (b1 + m).coerceIn(0.0, 1.0)
    )
}


fun transformToBooleanMatrix(
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

internal fun <T> writeOutputMatrixToPngFile(
    result: Matrix<T>,
    outputFile: File,
    colourProvider: (Int, Int, T) -> IntArray?
) {
    val bufferedImage = BufferedImage(
        result.numberOfRows,
        result.numberOfColumns,
        BufferedImage.TYPE_INT_RGB
    )

    result.forEachIndexed { row, column, value ->
        colourProvider(row, column, value).let {
            bufferedImage.raster.setPixel(row, column, it)
        }
    }

    ImageIO.write(bufferedImage, "png", outputFile)
}
