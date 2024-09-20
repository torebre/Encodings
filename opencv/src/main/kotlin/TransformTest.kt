import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.math.roundToInt


object TransformTest {

    init {
        // Need to also have this VM-option: -Djava.library.path=/home/student/projects/opencv/build/lib/
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    }

    fun loadAndShowImages() {
        val imageDir = Paths.get("/data/etlcdb-image-extractor/etl_data/images/ETL8G/0x9280")
        // TODO Just taking 5 for testing so far

        val filesToLoad = Files.walk(imageDir).filter {
            it.isRegularFile()
        }.toList()

        val imageMatrices = filesToLoad.take(5).map { ImageTransformMethods.transformImage(it) }

        val totalHeight = imageMatrices.sumOf { it.size().height }.roundToInt() + 1
        val totalWidth = imageMatrices.sumOf { it.size().width }.roundToInt() + 1

        val opencvMat = Mat(totalHeight, totalWidth, CvType.CV_8UC1, Scalar(0.0))

//        val cdst = Mat()
//        Imgproc.cvtColor(opencvMat, cdst, Imgproc.COLOR_GRAY2BGR)

        var rowSum = 1
        var columnSum =  1
        imageMatrices.forEach { imageMatrix ->
            imageMatrix.copyTo(opencvMat.rowRange(rowSum, rowSum + imageMatrix.height()).colRange(columnSum, columnSum + imageMatrix.width()))

            rowSum += imageMatrix.height()
            columnSum += imageMatrix.width()
        }

        HighGui.imshow("Source", opencvMat)
        // Wait and Exit
        HighGui.waitKey()
        System.exit(0)
    }


    @JvmStatic
    fun main(args: Array<String>) {
        loadAndShowImages()
    }


}