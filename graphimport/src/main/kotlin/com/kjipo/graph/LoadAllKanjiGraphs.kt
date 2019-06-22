package com.kjipo.graph


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal.Symbols.select
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.outE
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.`__`.select
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


        val graph = graphList[0]


//        println("Graph: ${graph.features()}")

        val traversals = graph.traversal()
                .withComputer()
//                .E()
                .V()
                .repeat(
                        outE().`as`("e")
                                .inV()).times(5)
//                .shortestPath()
                .path()
//                .by("e")
//                .select<Any>("e")
//                .values<Any>("category")
//                .outE("e_category")
//                .outE()
//                .path()
//                .by("category")
//                .group<Any, Any>()
//                .select<Any>("e")
//                .label()
//                .by(select<Any, Any>("e"))
//                        .values<Any>("category"))
//                .valueMap<Any>()
//                .toList()

        for (traversal in traversals) {
            val filteredPath = traversal.filter { it is Edge }
                    .map { it as Edge }
                    .map { graph.edges(it.id()).next().property<Any>("category").value() }

//            println("Traversal: $traversal")
            println("Filtered path: $filteredPath")
        }

    }


    @JvmStatic
    fun main(args: Array<String>) {
        loadAll()

    }


}