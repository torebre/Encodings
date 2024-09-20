package com.kjipo

import com.kjipo.experiments.MatrixVisualization
import com.kjipo.experiments.PointType
import com.kjipo.representation.Matrix
import javafx.scene.paint.Color


fun <T> createColorMatrix(matrixVisualization: MatrixVisualization<T>): Matrix<Color> {
    val colorMatrix =
        Matrix(matrixVisualization.matrix.numberOfRows, matrixVisualization.matrix.numberOfColumns) { _, _ ->
            Color.BLACK
        }

    matrixVisualization.matrix.forEachIndexed { row, column, value ->
        colorMatrix[row, column] = matrixVisualization.colorFunction(value)
            .let { rgbColor -> Color.color(rgbColor.red, rgbColor.green, rgbColor.blue) }
    }

    return colorMatrix
}


fun transformMatrixToColourArrays(matrix: Matrix<Color>) =
    Array(matrix.numberOfRows) { row ->
        Array(matrix.numberOfColumns) { column ->
            matrix[row, column]
        }
    }


fun transformPointTypeToColour(vararg pointType: PointType): Color {
    // TODO Should have better handling of multiple point types
    return pointType.map {
        when (it) {
            PointType.EMPTY -> Color.color(0.0, 0.0, 0.0)
            PointType.ENDPOINT -> Color.color(1.0, 0.0, 0.0)
            PointType.LINE -> Color.color(1.0, 1.0, 1.0)
        }
    }.last()
}

fun <T> transformToColourArrays(
    matrixVisualizations: Collection<MatrixVisualization<T>>,
    squareSize: Int = 1
): List<Matrix<Color>> {
    return matrixVisualizations.map {
        createColorMatrix(it)
    }
}
