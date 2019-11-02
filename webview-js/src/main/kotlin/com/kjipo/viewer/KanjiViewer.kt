import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Matrix
import com.kjipo.viewer.KanjiApp
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document


class KanjiViewer(
        private val bounds: Bounds,
        private val context: CanvasRenderingContext2D) {


    companion object {
        private const val SQUARE_SIZE = 50

    }


    fun drawKanji(matrix: Matrix<Int>) {
        console.log("Test24")

        setupKanjiDrawing(matrix, 2)

    }

    fun drawKanji(encodedKanji: EncodedKanji) {
        // TODO

        console.log("Test23")

//        context.lineWidth = 10.0
//        context.strokeRect(100.0, 100.0, 100.0, 100.0)

        setupKanjiDrawing(encodedKanji)

    }

    private fun setupKanjiDrawing(encodedKanji: EncodedKanji, squareSize: Int = SQUARE_SIZE) {
        var currentRow = 0


        encodedKanji.image.forEach { row ->
            var currentColumn = 0
            row.forEach { value ->
                if (value) {

                    console.log("Test24: ${currentRow.toDouble()}, ${currentColumn.toDouble()}")

                    context.fillRect(currentRow.toDouble(), currentColumn.toDouble(), squareSize.toDouble(), squareSize.toDouble())
                }
                ++currentColumn
            }
            ++currentRow
        }

//        rasters.forEach { colourRaster ->
//
//            //            if (texts.isNotEmpty()) {
////                val text = Text(currentColumn.toDouble() * colourRaster[0].size * squareSize, currentRow.toDouble() * colourRaster.size * squareSize, texts[rasterCounter])
//////                text.font = Font(20.0)
////                text.fill = Color.BLUE
////
////                rectangles.add(text)
////            }
//
//            val canvas = Canvas(colourRaster[0].size.toDouble() * squareSize + 1, colourRaster.size.toDouble() * squareSize + 1)
//            val gc = canvas.graphicsContext2D
//
//            canvas.layoutX = currentColumn * colourRaster[0].size.toDouble() * squareSize + currentColumn
//            canvas.layoutY = currentRow * colourRaster.size.toDouble() * squareSize + currentRow
//
//            gc.fill = Color.RED
//            gc.fillRect(0.0,
//                    0.0,
//                    colourRaster[0].size.toDouble() * squareSize + 1,
//                    colourRaster.size.toDouble() * squareSize + 1)
//
//            for (row in colourRaster.indices) {
//                for (column in 0 until colourRaster[0].size) {
//                    gc.fill = colourRaster[row][column]
//                    gc.fillRect(column * squareSize.toDouble(),
//                            row * squareSize.toDouble(),
//                            squareSize.toDouble(),
//                            squareSize.toDouble())
//                }
//            }
//            rectangles.add(canvas)
//
//            ++currentColumn
//            if (currentColumn == rastersPerLine) {
//                ++currentRow
//                currentColumn = 0
//            }
//
//            ++rasterCounter
//        }

    }

    private fun setupKanjiDrawing(matrix: Matrix<Int>, squareSize: Int = SQUARE_SIZE) {
        matrix.forEachIndexed { row, column, value ->
            if (value > 0) {
                console.log("Test24: ${row.toDouble()}, ${column.toDouble()}")
                context.fillRect(squareSize * row.toDouble(), squareSize * column.toDouble(), squareSize.toDouble(), squareSize.toDouble())
            }
        }
    }

}


data class Bounds(val xMin: Int, val yMin: Int, val xMax: Int, val yMax: Int)