package org.smolang.architecture

import java.util.Queue

/** requirement monitors */
abstract class Monitor(mName : String) : Entity(mName) {
    abstract fun check() : Boolean
    abstract fun getPort() : String
}

interface Stage{
    fun isMember(asset: Asset, KB : KnowledgeBase) : Boolean
    fun isConsistent(asset: Asset, mons : List<Entity>, KB : KnowledgeBase) : Boolean
    fun gen(asset: Asset) : List<Entity> //combines the two gen functions
    fun getKind() : String
}



/** asset controllers */
abstract class Controller(cName : String) : Entity(cName){
    abstract fun setOutput(newInput : Queue<Double>)
    abstract fun control()
    abstract fun getPort(): String
}

/* dummies for experiments */
class DefaultController(ctrlName : String) : Controller(ctrlName) {
    override fun setOutput(newInput : Queue<Double>){}
    override fun control() { /*println("${getName()} controls with $last")*/ }
    override fun getPort(): String = last.keys.firstOrNull() ?: ""
    override fun toString(): String = "[Controller (${getName()}) for ${getPort()}]"
}
