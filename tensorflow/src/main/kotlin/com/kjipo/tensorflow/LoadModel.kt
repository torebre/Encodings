package com.kjipo.tensorflow

import com.kjipo.parser.Parsers.JAPANESE_CHARSET
import com.kjipo.segmentation.Matrix
import com.kjipo.segmentation.shrinkImage
import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor
import sun.nio.cs.ext.DoubleByte
import sun.nio.cs.ext.JIS_X_0208
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

object LoadModel {


    private fun classifyImage(modelPath: Path) {
        val model = Model(modelPath)

        val readImage = ImageIO.read(File("test2.png"))
        val image = Matrix(readImage.height, readImage.width, { row, column -> false })
        for (row in 0 until readImage.height) {
            for (column in 0 until readImage.width) {
                if (readImage.getRGB(column, row) != -1) {
                    image[row, column] = true
                }
            }
        }

        val shrinkImage = shrinkImage(image, 32, 32)

        val inputData = Array(1, {
            Array(32, {
                Array(32, { index -> FloatArray(1) })

            })
        })

        shrinkImage.forEachIndexed { x, y, b ->
            inputData[0][x][y][0] = if (b) {
                1.0f
            } else {
                0.0f
            }
        }

        val inputTensor = Tensor.create(inputData, Float::class.javaObjectType)
        val outputData = model.classify(inputTensor)
        val maxValue = outputData.max()!!

        println("Max value: $maxValue")

        println("Index of max value: ${outputData.indexOf(maxValue)}")


        val outputString = outputData.joinToString(",")

        println(outputString)


    }


    @JvmStatic
    fun main(args: Array<String>) {
//        classifyImage(Paths.get("/home/student/workspace/kanji_python/saved_models/saved_model_2.pb"))

        val kanjiCode = 25124
        println(Integer.toBinaryString(kanjiCode))

        println(Integer.toBinaryString(0xF))
        val bitmask = 0xFF

        val secondByte = (bitmask and (kanjiCode shr 8)).toByte()
        val firstByte = (bitmask and kanjiCode).toByte()

//        println("""${Integer.toBinaryString(secondByte)}, ${Integer.toBinaryString(firstByte)}""")

        val byteArray = ByteArray(2)
        byteArray[0] = firstByte
        byteArray[1] = secondByte
        val byteBuffer = ByteBuffer.wrap(byteArray)

        val decoder = JIS_X_0208().newDecoder() as DoubleByte.Decoder

        println("Test25: ${decoder.decodeDouble((bitmask and (kanjiCode shr 8)), (bitmask and kanjiCode))}")


        println("Test23: ${JAPANESE_CHARSET.decode(byteBuffer)}")
        println("Test24: ${decoder.decode(byteBuffer)}")

    }


}