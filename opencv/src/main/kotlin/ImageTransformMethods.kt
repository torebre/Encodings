import com.kjipo.segmentation.Matrix
import com.kjipo.skeleton.makeSquare
import com.kjipo.skeleton.makeThin
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui
import org.opencv.imgproc.Imgproc
import java.nio.file.Path
import javax.imageio.ImageIO

object ImageTransformMethods {


    init {
        // Need to also have this VM-option: -Djava.library.path=/home/student/projects/opencv/build/lib/
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    }



    fun transformImage(image: Path): Mat {
        val readImage = ImageIO.read(image.toFile())

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

//        val dst = Mat()
//        Imgproc.Canny(opencvMat, dst, 20.0, 200.0, 3, false)

//        val cdst = Mat()
//        Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR)

//        val cdst = Mat()
//        Imgproc.cvtColor(opencvMat, cdst, Imgproc.COLOR_GRAY2BGR)

//        return cdst

        return opencvMat
    }


}