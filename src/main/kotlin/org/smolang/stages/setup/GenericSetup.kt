package org.smolang.stages.setup

import org.smolang.stages.architecture.*
import org.smolang.stages.declare.SemanticStage
import org.smolang.stages.system.Basil


abstract class GenericNVDIStage(var range: ClosedFloatingPointRange<Double>, val id : Int) : Stage {
    override fun isMember(candidate: Asset, KB: KnowledgeBase): Boolean {
        val va = KB.getValue(candidate, "n")
        return range.contains(va)
    }

    override fun isConsistent(name : Asset, mons : List<Entity>, KB : KnowledgeBase) : Boolean {
        return mons.isNotEmpty() && mons.all { it is GenericMonitor && it.id == id }
    }


    override fun getKind(): String = Basil
}

class GenericDeclareNVDIStage(range: ClosedFloatingPointRange<Double>, id: Int)
    : GenericNVDIStage( range, id ) {
    var count: Int = 0
    override fun gen(asset: Asset, KB: KnowledgeBase): List<Entity> {
        return listOf(GenericMonitor(range, asset,"rqFive_${count++}", id))
    }
}



class GenericSemanticNVDIStage(override var count: Int, range: ClosedFloatingPointRange<Double>, id: Int)
    : GenericNVDIStage( range, id ), SemanticStage {
        override fun gen(asset: Asset, KB : KnowledgeBase): List<Entity> = abduct(asset, KB, this)
}


class GenericMonitor(var range: ClosedFloatingPointRange<Double>, val assetName : Asset, monName : String, val id : Int) : Monitor(monName, "Req10Mon") {
    override fun check(): Boolean = last["m"] == null || range.contains(last["m"]!!)
    override fun getPort(): String = "m"
    override fun toString(): String = "[Monitor range $range (${getName()}) for $assetName]"
}