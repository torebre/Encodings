package com.kjipo.experiments


import com.kjipo.segmentation.Matrix
import com.kjipo.skeleton.makeSquare
import com.kjipo.skeleton.makeThin
import javafx.scene.paint.Color
import org.opencv.core.*
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO


private object HoughTransformExperiment {

    init {
        // Need to also have this VM-option: -Djava.library.path=/home/student/projects/opencv/build/lib/
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    }


    fun applyHoughTransformToDrawing(path: Path) {
        val readImage = ImageIO.read(path.toFile())

        val matrixImage = Matrix(readImage.height, readImage.width, { row, column -> false })
        for (row in 0 until readImage.height) {
            for (column in 0 until readImage.width) {
                if (readImage.getRGB(column, row) != -1) {

                    matrixImage[row, column] = true
                }
            }
        }

        val processedImage = makeThin(makeSquare(matrixImage))
        val opencvMat = Mat(processedImage.numberOfRows, processedImage.numberOfColumns, CvType.CV_8UC1, Scalar(0.0))

        processedImage.forEachIndexed({ row, column, value ->
            if (value) {
                val mr1 = opencvMat.row(row)
                val col = mr1.col(column)
                col.setTo(Scalar(255.0))
            }
        })

        val dst = Mat()
        Imgproc.Canny(opencvMat, dst, 20.0, 200.0, 3, false);

        val cdst = Mat()
        Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR)

        // Standard Hough Line Transform
        val lines = Mat() // will hold the results of the detection
        Imgproc.HoughLines(dst, lines, 1.0, Math.PI / 18, 30) // runs the actual detection

        // Draw the lines
        for (x in 0 until lines.rows()) {
            val rho = lines.get(x, 0)[0]
            val theta = lines.get(x, 0)[1]
            val a = Math.cos(theta)
            val b = Math.sin(theta)
            val x0 = a * rho
            val y0 = b * rho
            val pt1 = Point(Math.round(x0 + 1000 * -b).toDouble(), Math.round(y0 + 1000 * a).toDouble())
            val pt2 = Point(Math.round(x0 - 1000 * -b).toDouble(), Math.round(y0 - 1000 * a).toDouble())
            Imgproc.line(cdst, pt1, pt2, Scalar(0.0, 0.0, 255.0), 1, Imgproc.LINE_AA, 0)
        }

        // Show results
        HighGui.imshow("Source", opencvMat)
        HighGui.imshow("Detected Lines (in red) - Standard Hough Line Transform", cdst)
        // Wait and Exit
        HighGui.waitKey()
        System.exit(0)
    }


    @JvmStatic
    fun main(args: Array<String>) {
        applyHoughTransformToDrawing(Paths.get("test2.png"))
    }

}
