package com.kjipo.representation.prototype


interface AdjustablePrototype : Prototype {
    fun getMovements(): List<AdjustablePrototype>
}