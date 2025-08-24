package com.kjipo

import com.kjipo.ExperimentApplication.Companion.getColor
import com.kjipo.experiments.MatrixVisualization
import com.kjipo.representation.Matrix
import javafx.application.Application
import javafx.application.Application.launch
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage


class KanjiView : Application() {


    private fun drawFunction(
        canvas: Canvas,
        pointHeight: Double,
        pointWidth: Double,
        image: Matrix<Int>,
        colorFunction: (Int) -> Color
    ) {
        val gc = canvas.graphicsContext2D

        for (row in 0 until image.numberOfRows) {
            for (column in 0 until image.numberOfColumns) {
                image[row, column].let { value ->
                    gc.fill = colorFunction(value)
                    gc.fillRect(
                        column * pointWidth,
                        row * pointHeight,
                        pointWidth,
                        pointHeight
                    )
                }
            }
        }

    }

    override fun start(primaryStage: Stage?) {
        val matrixVisualizations = getMatrixVisualizationForExtractStrokes3()

        primaryStage?.let { stage ->
            var currentIndex = 0
        
            // Create a button
            val button = Button("Next")
        
            val root = VBox()
            root.children.add(button)

            val canvas = Canvas(300.0, 200.0)
            root.children.add(canvas)

            // Function to update canvas size and redraw
            fun updateCanvasAndRedraw() {
                // Reserve space for the button (approximate height)
                val buttonHeight = 30.0
                val newCanvasWidth = stage.width
                val newCanvasHeight = stage.height - buttonHeight
            
                // Update canvas size
                canvas.width = newCanvasWidth
                canvas.height = newCanvasHeight
            
                // Redraw the current matrix
                val currentVisualization = matrixVisualizations[currentIndex]
                val rows = currentVisualization.matrix.numberOfRows
                val columns = currentVisualization.matrix.numberOfColumns

                val pointHeight = newCanvasHeight / rows
                val pointWidth = newCanvasWidth / columns

                // Clear the canvas first
                canvas.graphicsContext2D.clearRect(0.0, 0.0, canvas.width, canvas.height)
            
                drawFunction(
                    canvas,
                    pointHeight,
                    pointWidth,
                    currentVisualization.matrix,
                    { value ->
                        getColor(currentVisualization.colorFunction(value))
                    }
                )
            }

            val currentVisualization = matrixVisualizations[currentIndex]

            // Set button action to go to next index
            button.setOnAction {
                currentIndex = (currentIndex + 1) % matrixVisualizations.size
                drawCurrentMatrix(currentVisualization, canvas)
            }

            // Add listeners for window size changes
            stage.widthProperty().addListener { _, _, _ -> updateCanvasAndRedraw() }
            stage.heightProperty().addListener { _, _, _ -> updateCanvasAndRedraw() }

            // Create and set the scene
            val scene = Scene(root, 300.0, 250.0)
            stage.scene = scene
            stage.title = "Kanji View"

            stage.show()
        
            // Draw initial matrix after showing to get correct window dimensions
            updateCanvasAndRedraw()
        }
    }

    private fun drawCurrentMatrix(matrixVisualization: MatrixVisualization<Int>, canvas: Canvas) {
        val rows = matrixVisualization.matrix.numberOfRows
        val columns = matrixVisualization.matrix.numberOfColumns

        val pointHeight = canvas.height / rows
        val pointWidth = canvas.width / columns

        // Clear the canvas first
        canvas.graphicsContext2D.clearRect(0.0, 0.0, canvas.width, canvas.height)

        drawFunction(
            canvas,
            pointHeight,
            pointWidth,
            matrixVisualization.matrix,
            { value ->
                getColor(matrixVisualization.colorFunction(value))
            }
        )
    }
}


fun main() {
    launch(KanjiView::class.java)
}
