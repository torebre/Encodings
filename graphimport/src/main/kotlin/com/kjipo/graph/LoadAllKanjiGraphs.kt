package com.kjipo.graph


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.outE
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import java.nio.file.Files
import java.nio.file.Paths


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


    @JvmStatic
    fun main(args: Array<String>) {
        loadAll()

    }


}