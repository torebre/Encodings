package com.kjipo.graph


import com.kjipo.experiments.LoadKanjiFromCsvFile
import com.kjipo.segmentation.Matrix
import com.kjipo.skeleton.transformToArrays
import com.kjipo.visualization.displayColourRasters
import javafx.scene.paint.Color
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.outE
import org.apache.tinkerpop.gremlin.structure.Direction
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.abs
import kotlin.streams.toList


object LoadAllKanjiGraphs {


    private fun loadAll() {
        val graphList = mutableListOf<TinkerGraph>()

        Files.list(Paths.get("/home/student/workspace/kanjiR/kanji_graphs2")).forEach { graphMlFile ->
            val newGraph = TinkerGraph.open()
            newGraph.traversal().io<Any>(graphMlFile.toAbsolutePath().toString())
                    .read()
                    .iterate()
            graphList.add(newGraph)
        }

        val pathCountMap = mutableMapOf<List<Int>, Int>()
        for (graph in graphList) {
            val traversals = graph.traversal()
                    .withComputer()
                    .V()
                    .repeat(
                            outE().`as`("e")
                                    .inV()).times(5)
                    .path()


            for (traversal in traversals) {
                val filteredPath = traversal.filter { it is Edge }
                        .map { it as Edge }
                        .map { graph.edges(it.id()).next().property<Double>("category").value() }
                        .map { it.toInt() }

//            println("Traversal: $traversal")
//            println("Filtered path: $filteredPath")

                pathCountMap.compute(filteredPath) { key, value ->
                    if (value == null) {
                        1
                    } else {
                        value + 1
                    }
                }
            }
        }

        pathCountMap.map {
            Pair(it.key, it.value)
        }
                .sortedBy { it.second }
                .forEach {
                    println("Key: ${it.first}. Value: ${it.second}")
                }
    }


    private fun traversePaths(): MutableMap<Path, MutableList<VertexPath>> {
        val allPaths = mutableListOf<List<PathElement>>()

        println("Examining graphs")

        val pathMap = mutableMapOf<Path, MutableList<VertexPath>>()

        // TODO Restricting the range while developing
//        for (graphMlFile in Files.list(Paths.get("/home/student/workspace/kanjiR/kanji_graphs3"))) {
        for (graphMlFile in Files.list(Paths.get("/home/student/workspace/kanjiR/kanji_graphs3")).limit(10)) {
            val fileName = graphMlFile.getName(graphMlFile.nameCount - 1).toString()
            val kanjiUnicode = fileName.substringBefore('.').toInt()

            val newGraph = TinkerGraph.open()
            newGraph.traversal().io<Any>(graphMlFile.toAbsolutePath().toString())
                    .read()
                    .iterate()


            val vertices = newGraph.vertices()
            val vertex = vertices.next()
            val seenVertices = mutableSetOf<Vertex>()

            seenVertices.add(vertex)

            val pathsToProcess = ArrayDeque<List<PathElement>>()
            pathsToProcess.add(listOf(PathElement(vertex, -1)))

            while (pathsToProcess.isNotEmpty()) {
                val pathToExpand = pathsToProcess.poll()
                val lastVertexOnPath = pathToExpand.last()

                val edges = lastVertexOnPath.vertex.edges(Direction.BOTH)

                for (edge in edges) {
                    val category = edge.property<Double>("category").orElseThrow {
                        throw IllegalStateException()
                    }.toInt()
                    for (verticesAtEdge in edge.bothVertices()) {
                        if (!seenVertices.contains(verticesAtEdge)) {
                            seenVertices.add(verticesAtEdge)
                            val extendedPath = pathToExpand + PathElement(verticesAtEdge, category)
                            allPaths.add(extendedPath)
                            pathsToProcess.add(extendedPath)
                        }
                    }
                }
            }

            for (path in allPaths) {
                val vertexList = path.map { it.vertex.property<Double>("lineNumber") }.map { it.orElseThrow({ java.lang.IllegalStateException("Expecting lineNumber property to be present") }).toInt() }.toList()

//            for(element in path) {
//                val lineNumber = element.vertex.property<Double>("lineNumber")
//                println("Line number: $lineNumber")
//            }

                val transformedPath = Path(path.stream().skip(1).map { it.category }.toList())


                if (pathMap.containsKey(transformedPath)) {
                    pathMap[transformedPath]?.add(VertexPath(vertexList, kanjiUnicode))
                } else {
                    pathMap[transformedPath] = mutableListOf(VertexPath(vertexList, kanjiUnicode))
                }

            }


        }


//        println("Transformed paths: $pathMap")

        return pathMap

    }


    private fun markLines(pathMap: MutableMap<Path, MutableList<VertexPath>>) {
        val kanjiData = Paths.get("kanji_data_full.csv")
        val unicodeKanjiMap = LoadKanjiFromCsvFile.readKanjiFile(kanjiData)

        val path = pathMap.entries.stream().filter {
            it.key.clusters.size > 3
        }
                .findAny().orElseThrow { throw java.lang.IllegalStateException("Expecting at least one path with length greater than 4") }

        val colourRasters = mutableListOf<Array<Array<Color>>>()
        val texts = mutableListOf<String>()
        val (minX, maxX, minY, maxY) = LoadKanjiFromCsvFile.findMinAndMax(unicodeKanjiMap)

        for (vertexPath in path.value) {
            val lineData = unicodeKanjiMap[vertexPath.kanjiUnicode]

            lineData?.let { lines ->
                var counter = 0
                val dispImage = Matrix(abs(maxX - minX), abs(maxY - minY)) { _, _ ->
                    Color.BLACK
                }

                lines.forEach { angleLine ->
                    angleLine.segments.flatMap { it.pairs }.forEach {
                        if (it.row >= 0 && it.row < dispImage.numberOfRows && it.column >= 0 && it.column < dispImage.numberOfColumns) {
//                            if (dispImage[it.row, it.column].brightness == 1.0) {

                            // TODO Does this cause the correct lines to be marked?
                            if (vertexPath.lineNumberList.contains(counter)) {
                                dispImage[it.row, it.column] = Color.hsb(counter.toDouble().div(lines.size).times(360), 1.0, 1.0)
                            } else {
                                dispImage[it.row, it.column] = Color.WHITE
                            }
                        }
                    }
                    ++counter
                }

                colourRasters.add(transformToArrays(dispImage))
                texts.add(vertexPath.kanjiUnicode.toString() + ": " + String(Character.toChars(vertexPath.kanjiUnicode)))
            }
        }

        displayColourRasters(colourRasters, texts, 2)

    }

    private data class PathElement(val vertex: Vertex, val category: Int)

    private data class VertexPath(val lineNumberList: List<Int>, val kanjiUnicode: Int)


    private data class Path(val clusters: List<Int>) {

        override fun hashCode(): Int {
            return clusters.sum()
        }


        override fun equals(other: Any?): Boolean {
            if (other !is Path) {
                return false
            }

            if (clusters.size != other.clusters.size) {
                return false
            }

            var match: Boolean
            for (i in 0 until clusters.size) {

                for (j in 0 until other.clusters.size) {
                    if (clusters[i] == other.clusters[j]) {
                        var index1 = i
                        var index2 = j

                        match = true
                        for (k in 0 until clusters.size) {
                            if (clusters[index1] != clusters[index2]) {
                                match = false
                                break
                            }

                            ++index1
                            ++index2

                            if (index1 == clusters.size) {
                                index1 = 0
                            }
                            if (index2 == other.clusters.size) {
                                index2 = 0
                            }
                        }
                        if (match) {
                            return true
                        }
                    }
                }


            }

            return false


        }


    }


    @JvmStatic
    fun main(args: Array<String>) {
//        loadAll()
        val pathMap = traversePaths()

        markLines(pathMap)

//        val path1 = Path(listOf(1, 2, 3))
//        val path2 = Path(listOf(1, 2, 3))
//        println(path1 == path2)
//        println("Test23")

    }


}