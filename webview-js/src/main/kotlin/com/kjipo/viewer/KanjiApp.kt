package com.kjipo.viewer

import Bounds
import KanjiViewer
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document


class KanjiApp {
    private val canvas = document.getElementById("canvas") as HTMLCanvasElement


    fun show() {
        canvas.height = 500
        canvas.width = 500

        val context = canvas.getContext("2d")

        console.log("Canvas: $canvas")
        console.log("Context: $context")

        val kanjiViewer = KanjiViewer(Bounds(0, 0, 500, 500),
                context as CanvasRenderingContext2D)

        kanjiViewer.drawKanji()
    }



}