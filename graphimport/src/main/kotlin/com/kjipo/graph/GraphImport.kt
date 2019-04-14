package com.kjipo.graph


import org.apache.tinkerpop.gremlin.structure.io.IoCore
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph

object GraphImport {


    @JvmStatic
    fun main(args: Array<String>) {
        val  newGraph = TinkerGraph.open()
        newGraph.io(IoCore.graphml()).readGraph("/home/student/workspace/kanjiR/test_graph.xml")



    }


}