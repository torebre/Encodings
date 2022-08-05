package com.kjipo

import kotlinx.browser.document
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.GenericSkikoView
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoView
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLCanvasElement


private class DemoApp : SkikoView {
    private val paint = Paint()

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        canvas.drawCircle(200f, 50f, 25f, paint)
        canvas.drawLine(100f, 100f, 200f, 200f, paint)

        canvas.drawRect(Rect(10f, 20f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
        canvas.drawOval(Rect(110f, 220f, 50f, 70f), paint)
    }
}

fun main() {
    console.log("Test23")
    onWasmReady {
        console.log("Test24")
        val skiaLayer = SkiaLayer()
        val canvas = document.getElementById("c1") as HTMLCanvasElement

        skiaLayer.skikoView = GenericSkikoView(skiaLayer, DemoApp())
        skiaLayer.attachTo(canvas)
        skiaLayer.needRedraw()

    }

}