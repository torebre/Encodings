package com.kjipo.graph


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.outE
import org.apache.tinkerpop.gremlin.structure.Direction
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


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


    private fun traversePaths() {
        val graphList = mutableListOf<TinkerGraph>()

        Files.list(Paths.get("/home/student/workspace/kanjiR/kanji_graphs2")).forEach { graphMlFile ->
            val newGraph = TinkerGraph.open()
            newGraph.traversal().io<Any>(graphMlFile.toAbsolutePath().toString())
                    .read()
                    .iterate()
            graphList.add(newGraph)
        }

        val allPaths = mutableListOf<List<Vertex>>()

        println("Examining graphs")

        // TODO Restricting the range while developing
        for (graph in graphList.subList(1, 10)) {
            val vertices = graph.vertices()
            val vertex = vertices.next()
            val seenVertices = mutableSetOf<Vertex>()


            seenVertices.add(vertex)

            val pathsToProcess = ArrayDeque<List<Vertex>>()
            pathsToProcess.add(listOf(vertex))

            while (pathsToProcess.isNotEmpty()) {
                val pathToExpand = pathsToProcess.poll()
                val lastVertexOnPath = pathToExpand.last()

                val edges = lastVertexOnPath.edges(Direction.BOTH)

                for (edge in edges) {
                    val category = edge.property<Double>("category").orElseThrow {
                        throw IllegalStateException()
                    }.toInt()
                    for (verticesAtEdge in edge.bothVertices()) {
                        if (!seenVertices.contains(verticesAtEdge)) {
                            seenVertices.add(verticesAtEdge)
                            val extendedPath = pathToExpand + verticesAtEdge

                            allPaths.add(extendedPath)

                            pathsToProcess.add(extendedPath)
                        }


                    }
                }
            }


        }

        println("All paths: $allPaths")


    }


    data class Path(val clusters: List<Int>) {

        override fun hashCode(): Int {
            return clusters.hashCode()
        }


        override fun equals(other: Any?): Boolean {
            if (other !is Path) {
                return false
            }

            if (clusters.size != other.clusters.size) {
                return false
            }

            var match = false
            search@ for (i in 0 until clusters.size) {

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
                            return@search
                        }
                    }
                }


            }

            return match


        }


    }


    @JvmStatic
    fun main(args: Array<String>) {
//        loadAll()
        traversePaths()

    }


}