package com.kjipo.tensorflow

import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor
import java.nio.file.Files
import java.nio.file.Path


internal class Model(modelPath: Path) {
    private val session: Session

    init {
        val allBytes = Files.readAllBytes(modelPath)
        val graph = Graph()

        graph.importGraphDef(allBytes)
        session = Session(graph)
    }

    fun classify(inputTensor: Tensor<Float>): FloatArray {
        val outputTensor = session.runner()
                .feed("conv2d_1_input", inputTensor)
                .fetch("dense_1/Softmax")
                .run()
                .get(0)

        val outputData = Array(1, { FloatArray(3036) })
        outputTensor.copyTo(outputData)

        return outputData[0]
    }

}