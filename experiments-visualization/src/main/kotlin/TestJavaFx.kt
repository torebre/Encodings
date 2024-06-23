package com.kjipo

import com.kjipo.ExperimentView.Companion.drawRasters
import com.kjipo.experiments.CircleLineTest
import javafx.application.Application
import javafx.application.Application.launch
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage

class TestJavaFx : Application() {
    val group: Group = Group()

    override fun start(primaryStage: Stage?) {
        instance = this

        primaryStage?.let { stage ->
            stage.title = "Hello World"
            val btn = Button()
            btn.text = "Say Hello World"
            btn.onAction = EventHandler { println("Hello World") }

            val root = StackPane()
            root.children.add(btn)
            stage.scene = Scene(root, 300.0, 250.0)

            // TODO Just here for testing
            root.children.add(group)

            stage.show()
        }

    }


    fun loadRasters(
        colourRasters: Collection<Array<Array<Color>>>,
        texts: List<String> = emptyList(),
        squareSize: Int = 1
    ) {
        group.children.clear()
        drawRasters(group.children, texts, squareSize, colourRasters)
    }

    companion object {
        var instance: TestJavaFx? = null

    }

}

fun main(args: Array<String>) {
    val startThread = Thread {
        launch(TestJavaFx::class.java, *args)
    }
    startThread.start()

    val circleLineTest = CircleLineTest()
    val matrixImages = circleLineTest.extractLineSegments(32769).map {
        createColorMatrix(it)
    }
        .map {
            transformMatrixToColourArrays(it)
        }

    Platform.runLater {
        TestJavaFx.instance?.loadRasters(matrixImages)
    }

}
