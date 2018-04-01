package com.kjipo.skeleton

import com.kjipo.representation.EncodedKanji
import com.kjipo.segmentation.ColorCell2
import com.kjipo.segmentation.ColorPainter2
import com.kjipo.segmentation.Matrix
import com.kjipo.visualization.RasterRun
import com.kjipo.visualization.RasterVisualizer2
import javafx.scene.paint.Color
import org.junit.Test
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths


class BwmethodsTest {


    @Test
    fun fillHolesTest() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, { row, column -> encodedKanji.image[row][column] })

        println("Encoded kanji: $encodedKanji")

        val outputImage = binaryFillHoles(image)

        val colorImage = Array(encodedKanji.image.size, { row ->
            Array(encodedKanji.image[0].size, { column ->

                if (outputImage.get(row, column)) {
                    Color.RED
                } else if (image[row, column]) {
                    Color.YELLOW
                } else {
                    Color.BLACK
                }
            })
        })

        RasterVisualizer2.paintRaster(colorImage, 5)

        Thread.sleep(Long.MAX_VALUE)

    }

    @Test
    fun makeThinTest() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, { row, column -> encodedKanji.image[row][column] })

        println("Encoded kanji: $encodedKanji")

        val outputImage = makeThin(image)

        val colorImage = Array(encodedKanji.image.size, { row ->
            Array(encodedKanji.image[0].size, { column ->

                if (outputImage.get(row, column)) {
                    Color.RED
                } else if (image[row, column]) {
                    Color.YELLOW
                } else {
                    Color.BLACK
                }
            })
        })

        RasterVisualizer2.paintRaster(colorImage, 5)

        Thread.sleep(Long.MAX_VALUE)
    }


    @Test
    fun makeThinTest2() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, { row, column -> encodedKanji.image[row][column] })

        println("Encoded kanji: $encodedKanji")

        val outputImage = makeThin(image)

        val colorImage = Array(encodedKanji.image.size, { row ->
            Array(encodedKanji.image[0].size, { column ->

                if (outputImage.get(row, column)) {
                    Color.RED
                } else if (image[row, column]) {
                    Color.YELLOW
                } else {
                    Color.BLACK
                }
            })
        })

        RasterVisualizer2.paintRaster(colorImage, 5)

        Thread.sleep(Long.MAX_VALUE)
    }


    @Test
    fun applyTest() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, { row, column -> encodedKanji.image[row][column] })

        var previous = image
        var result = Matrix.copy(image)
        var change: Boolean

        var colourRasters = mutableListOf<Array<Array<Color>>>()

        do {
            change = false
            applyLookup(applyLookup(previous, lookup1), lookup2).forEachIndexed({ row, column, value ->
                val temp = previous[row, column]
                result[row, column] = previous[row, column] && value

                if (temp != result[row, column]) {
                    change = true
                }
            })

            val colorImage = Array(encodedKanji.image.size, { row ->
                Array(encodedKanji.image[0].size, { column ->
                    if (result.get(row, column)) {
                        Color.RED
                    } else if (image[row, column]) {
                        Color.YELLOW
                    } else {
                        Color.BLACK
                    }
                })
            })
            colourRasters.add(colorImage)

            previous = result

        } while (change)

        showRasters(encodedKanji.image, colourRasters)

        Thread.sleep(Long.MAX_VALUE)

    }


    @Test
    fun testThin3() {
        val image = Matrix(2, 2, { row, column -> false })

        val colorImage = Array(2, { row ->
            Array(2, { column ->
                if (row == 0 || (row == 1 && column == 1)) {
                    image[row, column] = true
                    Color.RED
                } else {
                    image[row, column] = false
                    Color.BLACK
                }
            })
        })

        println("Lookup1: ${lookup1.size}. Lookup2: ${lookup2.size}")

        RasterVisualizer2.paintRaster(colorImage, 50)

        val previous = Matrix.copy(image)
        val result = Matrix.copy(image)
//        applyLookup(applyLookup(previous, lookup1), lookup2).forEachIndexed({row, column, value ->
//            result[row, column] = previous[row, column] && value
//        })
        applyLookup(previous, lookup1).forEachIndexed({ row, column, value ->
            result[row, column] = value
        })

        val colorImage2 = Array(2, { row ->
            Array(2, { column ->
                if (row == 0 || (row == 1 && column == 1)) {
                    image[row, column] = true
                    Color.RED
                } else {
                    image[row, column] = false
                    Color.BLACK
                }
            })
        })
        RasterVisualizer2.paintRaster(colorImage2, 50)

        Thread.sleep(Long.MAX_VALUE)
    }

    @Test
    fun makeThinTest4() {
        val image = Matrix(2, 2, { row, column -> false })
        image[0, 0] = true
        image[0, 1] = true
        image[1, 1] = true


        println("lookup1 size: ${lookup1.size}. lookup2 size: ${lookup2.size}")

//        thin2(image)
        val outputImage = applyLookup(image, lookup1)

        val colorImage = Array(image.numberOfRows, { row ->
            Array(image.numberOfColumns, { column ->

                if (outputImage.get(row, column)) {
                    Color.RED
                } else if (image[row, column]) {
                    Color.YELLOW
                } else {
                    Color.BLACK
                }
            })
        })

        RasterVisualizer2.paintRaster(colorImage, 50)

        Thread.sleep(Long.MAX_VALUE)
    }


    @Test
    fun fillIsolatedHolesTest() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, { row, column -> encodedKanji.image[row][column] })
        val outputImage = fillIsolatedHoles(image)

        val colorImage = Array(encodedKanji.image.size, { row ->
            Array(encodedKanji.image[0].size, { column ->

                if (outputImage.get(row, column)) {
                    Color.RED
                } else if (image[row, column]) {
                    Color.YELLOW
                } else {
                    Color.BLACK
                }
            })
        })

        RasterVisualizer2.paintRaster(colorImage, 5)

        Thread.sleep(Long.MAX_VALUE)
    }


    @Test
    fun findEndpointsTest() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        val image = Matrix(encodedKanji.image.size, encodedKanji.image[0].size, { row, column -> encodedKanji.image[row][column] })

        println("Encoded kanji: $encodedKanji")

        val thinnedImage = makeThin(image)
        val outputImage = bwmorphEndpoints(thinnedImage)

        val colorImage = Array(encodedKanji.image.size, { row ->
            Array(encodedKanji.image[0].size, { column ->

                if (outputImage.get(row, column)) {
                    Color.RED
                } else if (thinnedImage[row, column]) {
                    Color.GREEN
                } else if (image[row, column]) {
                    Color.YELLOW
                } else {
                    Color.BLACK
                }
            })
        })


        RasterVisualizer2.paintRaster(colorImage, 5)

//        RasterVisualizer2.paintRaster(encodedKanji.image, 5)
//        val outputImage2 = thinNotWorking(image)
//        val colorImage2 = Array(encodedKanji.image.size, { row ->
//            Array(encodedKanji.image[0].size, { column ->
//                if (outputImage2.get(row, column)) {
//                    Color.WHITE
//                } else {
//                    Color.BLACK
//                }
//            })
//        })
//        RasterVisualizer2.paintRaster(colorImage2, 5)


        Thread.sleep(Long.MAX_VALUE)


    }


    @Test
    fun writeTestKanjiToMatrixFile() {
        val encodedKanji = FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile())
                .use { ObjectInputStream(it).use { it.readObject() as EncodedKanji } }

        Files.newBufferedWriter(Paths.get("/home/student/Documents/test_kanji.csv"), StandardCharsets.UTF_8).use {
            val writer = it
            encodedKanji.image.forEach {
                it.forEach {
                    writer.write(if (it) {
                        "1"
                    } else {
                        "0"
                    })
                    writer.write(",")
                }
                writer.newLine()
            }
        }
    }


    private fun showRasters(rawInput: Array<BooleanArray>, colourRasters: List<Array<Array<Color>>>) {
        RasterVisualizer2.showRasterFlow<ColorCell2>(
                object : RasterRun<ColorCell2> {
                    private var current = -1

                    override fun getRawInput() = rawInput

                    override fun hasNext() = current < colourRasters.size - 1

                    override fun getColumns() = colourRasters[0].size

                    override fun getRows() = colourRasters[0][0].size

                    override fun getCell(row: Int, column: Int) = ColorCell2(row, column, colourRasters.get(current)[row][column])

                    override fun next() {
                        ++current
                    }
                },
                listOf(ColorPainter2()), 5)
    }


}