import com.kjipo.representation.EncodedKanji
import com.kjipo.representation.Matrix
import com.kjipo.viewer.KanjiApp
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document


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

    fun setupKanjiDrawing(matrix: Matrix<Int>, squareSize: Int = SQUARE_SIZE) {
        matrix.forEachIndexed { row, column, value ->
            if (value > 0) {
                context.fillStyle = "#${value.toString(16)}FFFF"
                context.fillRect(squareSize * row.toDouble(), squareSize * column.toDouble(), squareSize.toDouble(), squareSize.toDouble())
            }
        }
    }

}
