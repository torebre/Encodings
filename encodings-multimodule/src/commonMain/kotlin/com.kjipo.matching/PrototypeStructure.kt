package com.kjipo.matching

import com.kjipo.representation.prototype2.AppliedPrototype

class PrototypeStructure {
    private val appliedPrototypes = mutableListOf<AppliedPrototype>()


    fun addPrototype(appliedPrototype: AppliedPrototype) {
        appliedPrototypes.add(appliedPrototype)
    }



}