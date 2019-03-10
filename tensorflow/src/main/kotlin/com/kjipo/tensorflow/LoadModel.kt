package com.kjipo.tensorflow

import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object LoadModel {


    private fun loadModel(modelPath: Path) {
        val allBytes = Files.readAllBytes(modelPath)
        val graph = Graph()

        graph.importGraphDef(allBytes)

        val session = Session(graph)
        val inputData = Array(1, {
            Array(28, {
                Array(28, { index -> FloatArray(1) })

            })


        })

        val inputTensor = Tensor.create(inputData, Float::class.javaObjectType)
        val outputTensor = session.runner()
                .feed("conv2d_1_input", inputTensor)
                .fetch("dense_1/Softmax")
                .run()
                .get(0)



        val outputData = Array(1, { FloatArray(3036) })
        outputTensor.copyTo(outputData)

        outputData[0].let {

            val maxValue = it.max()!!

            println("Max value: $maxValue")

            println("Index of max value: ${it.indexOf(maxValue)}")


            val outputString = it.joinToString(",")

            println(outputString)

        }


    }


    @JvmStatic
    fun main(args: Array<String>) {
        loadModel(Paths.get("/home/student/workspace/kanji_python/cnn/saved_model.pb"))


    }


}