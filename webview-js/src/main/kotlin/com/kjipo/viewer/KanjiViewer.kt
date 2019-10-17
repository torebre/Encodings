import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document


class KanjiViewer(
        private val bounds: Bounds,
        private val context: CanvasRenderingContext2D) {


    fun drawKanji() {
        // TODO

        console.log("Test23")

        context.lineWidth = 10.0
        context.strokeRect(100.0, 100.0, 100.0, 100.0)



    }


}


data class Bounds(val xMin: Int, val yMin: Int, val xMax: Int, val yMax: Int)