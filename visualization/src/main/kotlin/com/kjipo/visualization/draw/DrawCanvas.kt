package com.kjipo.visualization.draw

import javafx.embed.swing.SwingFXUtils
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO


class DrawCanvas : View("Draw") {
    override val root = vbox {
        var graphicsContext2D: GraphicsContext? = null

        canvas {
            width = 300.0
            height = 250.0

            graphicsContext2D = this.graphicsContext2D
            graphicsContext2D?.fill = Color.WHITE
            graphicsContext2D?.stroke = Color.BLACK

            addEventHandler(MouseEvent.MOUSE_PRESSED,
                    {
                        graphicsContext2D?.beginPath()
                        graphicsContext2D?.moveTo(it.x, it.y)
                        graphicsContext2D?.stroke()
                    })

            addEventHandler(MouseEvent.MOUSE_DRAGGED,
                    {
                        graphicsContext2D?.lineTo(it.x, it.y)
                        graphicsContext2D?.stroke()
                        graphicsContext2D?.closePath()
                        graphicsContext2D?.beginPath()
                        graphicsContext2D?.moveTo(it.x, it.y)
                    })

            addEventHandler(MouseEvent.MOUSE_RELEASED,
                    {
                        graphicsContext2D?.lineTo(it.x, it.y)
                        graphicsContext2D?.stroke()
                        graphicsContext2D?.closePath()
                    })

        }


        hbox {
            button("Save with auto name").setOnAction {
                graphicsContext2D?.let {
                    val wi = WritableImage(it.canvas.width.toInt(), it.canvas.height.toInt())
                    val snapshot = it.canvas.snapshot(SnapshotParameters(), wi)

                    val output = File("snapshot" + Date().getTime() + ".png")
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output)


                }
            }

            button("Save").setOnAction {
                val fileChooser = FileChooser()
                fileChooser.title = "Save Image"
                val file = fileChooser.showSaveDialog(this@DrawCanvas.currentWindow)
                if (file != null) {
                    graphicsContext2D?.let {
                        try {
                            val wi = WritableImage(it.canvas.width.toInt(), it.canvas.height.toInt())
                            val snapshot = it.canvas.snapshot(SnapshotParameters(), wi)
                            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", if (file.name.endsWith(".png")) {
                                file
                            } else {
                                File(file.absoluteFile.toString() + ".png")
                            })

                        } catch (ex: IOException) {
                            println(ex.message)
                        }
                    }
                }
            }

            button("Clear").setOnAction {
                graphicsContext2D?.let {
                    it.clearRect(0.0, 0.0, it.canvas.width, it.canvas.height)
                }
            }
        }


    }

}