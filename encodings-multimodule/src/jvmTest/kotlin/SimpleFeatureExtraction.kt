import com.kjipo.representation.Matrix
import com.kjipo.representation.raster.FlowDirection
import com.kjipo.representation.raster.getFlowDirectionForOffset
import com.kjipo.representation.raster.getNeighbourhood
import com.kjipo.representation.raster.makeSquare
import com.kjipo.segmentation.RegionExtractor
import com.kjipo.segmentation.getOffset

/**
 * Extract some simple features from a kanji image and
 * store them in a way so that the spatial relation between
 * the features can be matched between the input image and
 * other images.
 */
object SimpleFeatureExtraction {


    fun runSimpleFeatureExtraction() {
        val dataset = loadImagesFromEtl9G(5).first()

        val imageMatrix = makeSquare(transformToBooleanMatrix(dataset.kanjiData))
        val regionExtractor = RegionExtractor()

        val edgePixels = regionExtractor.markEdgePixels(imageMatrix)
        val edgePixelsMatrix = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { _, _ -> false }
        edgePixels.forEach {
            edgePixelsMatrix[it.first, it.second] = true
        }

        val result = Matrix(imageMatrix.numberOfRows, imageMatrix.numberOfColumns) { _, _ -> 0 }

        for (row in 0 until imageMatrix.numberOfRows) {
            for (column in 0 until imageMatrix.numberOfColumns) {
                if (edgePixelsMatrix[row, column]) {
                    result[row, column] = 1
                } else if (imageMatrix[row, column]) {
                    result[row, column] = 2
                }
            }
        }

        val matrixCopy = Matrix.copy(edgePixelsMatrix)
        val numberOfEdgePixels = edgePixels.size

        val pathMatrix =
            Matrix<FlowDirection?>(edgePixelsMatrix.numberOfRows, edgePixelsMatrix.numberOfColumns) { _, _ ->
                null
            }

        for (row in 0 until matrixCopy.numberOfRows) {
            for (column in 0 until matrixCopy.numberOfColumns) {
                if (matrixCopy[row, column]) {
                    val neighbourhood = getNeighbourhood(matrixCopy, row, column)

                    neighbourhood.forEachIndexed { innerRow, innerColumn, value ->
                        if (value && pathMatrix[row, column] == null) {
                            val rowOffset = getOffset(innerRow)
                            val columnOffset = getOffset(innerColumn)

                            pathMatrix[row, column] = getFlowDirectionForOffset(rowOffset, columnOffset)

                            // TODO

                        }
                    }

                }

            }
        }

    }


    @JvmStatic
    fun main(args: Array<String>) {
        runSimpleFeatureExtraction()
    }


}