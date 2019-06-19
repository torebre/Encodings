package com.kjipo.graph

import org.apache.tinkerpop.gremlin.process.traversal.IO
import org.apache.tinkerpop.gremlin.process.traversal.IO.graphml
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.T
import org.apache.tinkerpop.gremlin.structure.io.IoCore
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import java.nio.file.Files
import java.nio.file.Paths


object LoadAllKanjiGraphs {


    private fun loadAll() {
        val graphList = mutableListOf<TinkerGraph>()

        Files.list(Paths.get("/home/student/workspace/kanjiR/kanji_graphs")).forEach { graphMlFile ->
            val newGraph = TinkerGraph.open()
            newGraph.traversal().io<Any>(graphMlFile.toAbsolutePath().toString())
                    .read()
                    .iterate()
            graphList.add(newGraph)
        }


        val graph = graphList[0]

//        println("Graph: ${graph.features()}")

        val traversals = graph.traversal().withComputer().V().shortestPath().toList()


        println(traversals)


    }


    @JvmStatic
    fun main(args: Array<String>) {
        loadAll()

    }


}