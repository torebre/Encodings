import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Matrix
import org.w3c.dom.CanvasRenderingContext2D


class KanjiViewer(private val context: CanvasRenderingContext2D) {

    companion object {
        const val SQUARE_SIZE = 50
    }

    fun setupKanjiDrawing(encodedKanji: EncodedKanji, squareSize: Int = SQUARE_SIZE) {
        var currentRow = 0

        encodedKanji.image.forEach { row ->
            var currentColumn = 0
            row.forEach { value ->
                if (value) {
                    context.fillRect(currentRow.toDouble(), currentColumn.toDouble(), squareSize.toDouble(), squareSize.toDouble())
                }
                ++currentColumn
            }
            ++currentRow
        }
    }

    fun setupKanjiDrawing(matrix: Matrix<Int>, squareSize: Int = SQUARE_SIZE, useColours: Boolean = false) {
        if (useColours) {
            val colours = mutableSetOf<Int>()
            matrix.forEach { colours.add(it) }

            var hueChange = 360 / colours.size
            if (hueChange == 0) {
                hueChange = 1
            }

            val colourMap = colours.map { value ->
                Pair(value, hsvToRgb(value * hueChange, 1.0, 1.0))
            }.toMap()

            matrix.forEachIndexed { row, column, value ->
                if (value > 0) {
                    context.fillStyle = colourMap[value]?.toHexadecimalString()
                    context.fillRect(squareSize * row.toDouble(), squareSize * column.toDouble(), squareSize.toDouble(), squareSize.toDouble())
                }
            }
        } else {
            matrix.forEachIndexed { row, column, value ->
                if (value > 0) {
                    val hexadecimalValue = value.toString(16)
                    context.fillStyle = "#${hexadecimalValue}${hexadecimalValue}${hexadecimalValue}"
                    context.fillRect(squareSize * row.toDouble(), squareSize * column.toDouble(), squareSize.toDouble(), squareSize.toDouble())
                }
            }

        }

    }

    class RgbColour(val red: Int, val green: Int, val blue: Int) {

        constructor(red: Double, green: Double, blue: Double) :
                this(red.toInt(), green.toInt(), blue.toInt())

        fun toHexadecimalString(): String {
            val redFormatted = addLeadingZeroIfNecessary(red.toString(16))
            val greenFormatted = addLeadingZeroIfNecessary(green.toString(16))
            val blueFormatted = addLeadingZeroIfNecessary(blue.toString(16))

            return "#${redFormatted}${greenFormatted}${blueFormatted}"
        }

        private fun addLeadingZeroIfNecessary(number: String) =
                if (number.length == 1) {
                    "0$number"
                } else {
                    number
                }
    }


    fun hsvToRgb(hue: Int, sat: Double, value: Double): RgbColour {
//        hue %= 360;

        var hueInternal = hue.rem(360)
        var satInternal = sat
        var valueInternal = value

        while (hueInternal < 0) {
            hueInternal += 360
        }

        if (satInternal < 0.0) {
            satInternal = 0.0
        }
        if (satInternal > 1.0) {
            satInternal = 1.0
        }

        if (valueInternal < 0.0) {
            valueInternal = 0.0
        }
        if (valueInternal > 1.0) {
            valueInternal = 1.0
        }

        val h = hueInternal / 60
        val f = hueInternal.toDouble() / 60 - h
        val p = value * (1.0 - satInternal)
        val q = value * (1.0 - satInternal * f)
        val t = value * (1.0 - satInternal * (1 - f))

        return when (h) {
            0, 6 -> RgbColour(valueInternal * 255, t * 255, p * 255)
            1 -> RgbColour(q * 255, valueInternal * 255, p * 255)
            2 -> RgbColour(p * 255, valueInternal * 255, t * 255)
            3 -> RgbColour(p * 255, valueInternal * 255, t * 255)
            4 -> RgbColour(t * 255, p * 255, valueInternal * 255)
            5 -> RgbColour(valueInternal * 255, p * 255, q * 255)
            else -> RgbColour(valueInternal * 255, t * 255, p * 255)
        }
    }

}
