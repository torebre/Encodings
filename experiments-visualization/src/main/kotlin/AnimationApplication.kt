package com.kjipo


import com.kjipo.experiments.*
import com.kjipo.representation.Matrix
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.min


private val logger = System.getLogger(ExperimentApplication::class.qualifiedName!!)

class AnimationApplication : Application() {
    private var root: StackPane? = null
    private var matrix: Matrix<Color>? = null
    private var updateFunction: ((Matrix<Color>, Int) -> Boolean)? = null


    override fun start(primaryStage: Stage?) {
        primaryStage?.let { stage ->
            root = StackPane().also {
                stage.scene = Scene(it, 300.0, 250.0)
            }

            stage.widthProperty().addListener { _, _, _ -> drawRasters() }
            stage.heightProperty().addListener { _, _, _ -> drawRasters() }
            stage.show()
        }

        instance = this
    }


    private fun drawRasters() {
        drawRasters(root?.height ?: 100.0, root?.width ?: 100.0)
    }

    private fun drawRasters(height: Double, width: Double) {
        root?.children?.clear()

        val rectangles = Group()
        val kanjiImageCanvas = Canvas(width, height)
        val totalLengthAvailable = min(width, height)

        matrix?.let { it ->
            val squareSize = totalLengthAvailable / it.numberOfRows
            drawMatrix(it, squareSize, kanjiImageCanvas.graphicsContext2D)

            kanjiImageCanvas.layoutX = squareSize
            kanjiImageCanvas.layoutY = squareSize

            rectangles.children.add(kanjiImageCanvas)

            root?.children?.add(rectangles)
        }

    }

    private fun drawMatrix(matrix: Matrix<Color>, squareSize: Double, gc: GraphicsContext) {
        for (row in 0 until matrix.numberOfRows) {
            for (column in 0 until matrix.numberOfColumns) {
                matrix[row, column].let { colour ->
                    gc.fill = colour
                    gc.fillRect(
                        column * squareSize,
                        row * squareSize,
                        squareSize,
                        squareSize
                    )
                }
            }
        }

    }


    companion object {
        var instance: AnimationApplication? = null


        fun startAnimation() {
            val matrix = Matrix(10, 10) { _, _ -> Color.BLACK }
            val updateFunction = { matrix: Matrix<Color>, counter: Int ->

                if (counter > matrix.numberOfRows * matrix.numberOfColumns) {
                    false
                } else {
                    val columnNumber = counter % matrix.numberOfColumns
                    val rowNumber = counter / matrix.numberOfColumns

                    logger.log(System.Logger.Level.INFO, "Row: $rowNumber, column: $columnNumber")

                    matrix[rowNumber, columnNumber] = Color.WHITE

                    true
                }
            }

            val startThread = Thread {
                launch(AnimationApplication::class.java)
            }
            startThread.start()

            var counter = 0
            do {
                if (counter > 10) {
                    throw RuntimeException("Application did not start in 10 seconds")
                }
                Thread.sleep(500)
                ++counter
            } while (instance == null)

            instance?.let {
                it.matrix = matrix
                it.updateFunction = updateFunction
            }

            runAnimation()
        }

        private fun runAnimation() {
            var counter = 0

            do {
                val animationContinue = instance?.let { it ->
                    it.matrix?.let { matrix ->
                        it.updateFunction?.invoke(matrix, counter)
                    }
                }

                logger.log(System.Logger.Level.INFO, "Animation step: $counter. Animation continue: $animationContinue")

                if (animationContinue != null && animationContinue) {
                    Platform.runLater {
                        instance?.drawRasters()
                    }
                    ++counter
                }

                Thread.sleep(1000)

            } while (animationContinue != null && animationContinue)


        }


        private fun getColor(pointColor: PointColor): Color {
            return Color.color(pointColor.red, pointColor.green, pointColor.blue)
        }

    }


}
