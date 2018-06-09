import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.CvType
import org.opencv.core.Scalar

internal object SimpleSample {

    init {
        // Need to also have this VM-option: -Djava.library.path=/home/student/projects/opencv/build/lib/
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        System.out.println("Welcome to OpenCV " + Core.VERSION)
        val m = Mat(5, 10, CvType.CV_8UC1, Scalar(0.0))
        println("OpenCV Mat: $m")
        val mr1 = m.row(1)
        mr1.setTo(Scalar(1.0))
        val mc5 = m.col(5)
        mc5.setTo(Scalar(5.0))
        System.out.println("OpenCV Mat data:\n" + m.dump())
    }

}
