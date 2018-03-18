package com.kjipo.skeleton

import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.Matrix
import com.kjipo.visualization.RasterVisualizer2
import javafx.scene.paint.Color
import org.junit.Test
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.nio.file.Paths


class BwmethodsTest {


    @Test
    fun fillHolesTest() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, {row, column -> encodedKanji.image[row][column]})

        println("Encoded kanji: $encodedKanji")

        val outputImage = binaryFillHoles(image)

        val colorImage = Array(encodedKanji.image.size, {row ->
            Array(encodedKanji.image[0].size, {column -> if(outputImage.get(row, column)) {
                Color.WHITE
            }
            else {
                Color.BLACK
            }})
        })


        RasterVisualizer2.paintRaster(encodedKanji.image)

        RasterVisualizer2.paintRaster(colorImage)


        Thread.sleep(Long.MAX_VALUE)

    }

    @Test
    fun makeThinTest() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, {row, column -> encodedKanji.image[row][column]})

        println("Encoded kanji: $encodedKanji")

        val outputImage = makeThin(image)

        val colorImage = Array(encodedKanji.image.size, {row ->
            Array(encodedKanji.image[0].size, {column -> if(outputImage.get(row, column)) {
                Color.WHITE
            }
            else {
                Color.BLACK
            }})
        })


        RasterVisualizer2.paintRaster(encodedKanji.image)

        RasterVisualizer2.paintRaster(colorImage)


        val outputImage2 = thin(image)
        val colorImage2 = Array(encodedKanji.image.size, {row ->
            Array(encodedKanji.image[0].size, {column -> if(outputImage2.get(row, column)) {
                Color.WHITE
            }
            else {
                Color.BLACK
            }})
        })

        RasterVisualizer2.paintRaster(colorImage2)



        Thread.sleep(Long.MAX_VALUE)

    }


}