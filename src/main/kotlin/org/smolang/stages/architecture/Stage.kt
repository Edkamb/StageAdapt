package org.smolang.stages.architecture

import java.util.Queue

/** requirement monitors */
abstract class Monitor(override val nName: String, override val nKind: String) : Entity(nName, nKind) {
    abstract fun check() : Boolean
    abstract fun getPort() : String
}

interface Stage{
    fun isMember(asset: Asset, KB : KnowledgeBase) : Boolean{
        return isMember(setOf(asset),KB)
    }
    fun isMember(assets: Set<Asset>, KB : KnowledgeBase) : Boolean
    fun isConsistent(mons : List<Entity>, KB : KnowledgeBase) : Boolean
    fun gen(asset: Asset, KB : KnowledgeBase) : List<Entity>
    fun getKind() : String
}

class DirectCompose(val stage1: Stage, val stage2: Stage,
                    val size1 : Int, val size2 : Int,
                    val genCompose : (asset :Asset, KB: KnowledgeBase) -> List<Entity>) : Stage {

    override fun isMember(assets: Set<Asset>, KB : KnowledgeBase) : Boolean{
        return assets.windowed(size1).any { stage1.isMember(it.toSet(), KB) } && assets.windowed(size2).any { stage2.isMember(it.toSet(), KB) }
    }
    override fun isConsistent(mons : List<Entity>, KB : KnowledgeBase) : Boolean{
        return stage1.isConsistent(mons, KB) && stage2.isConsistent(mons, KB)
    }
    override fun gen(asset: Asset, KB : KnowledgeBase) : List<Entity> = genCompose(asset, KB)
    override fun getKind() : String = stage1.getKind()+"."+stage2.getKind()
}

/** asset controllers */
abstract class Controller(override val nName: String, override val nKind: String) : Entity(nName, nKind) {
    abstract fun setOutput(newInput : Queue<Double>)
    abstract fun control()
    abstract fun getPort(): String
}

/* dummies for experiments */
class DefaultController(ctrlName : String) : Controller(ctrlName, "CTRL") {
    override fun setOutput(newInput : Queue<Double>){}
    override fun control() { /*println("${getName()} controls with $last")*/ }
    override fun getPort(): String = last.keys.firstOrNull() ?: ""
    override fun toString(): String = "[Controller (${getName()}) for ${getPort()}]"
}
