package org.smolang.stages.declare

import org.smolang.stages.architecture.*
import org.smolang.stages.system.*

interface SemanticStage {
    var count: Int

    fun abduct(asset: Asset, KB : KnowledgeBase, stage: Stage): List<Entity>{
        /* Get all my monitors */
        val ents : List<Entity> = KB.getAssigned(asset)

        /* now delete them */
        for (e in ents)
            KB.removeEntity(e)

        /* get which classes we have */
        val classes : List<String> = KB.getPossibleEntities()
        val clazzes = classes.map { entityToClass[it] }

        /* now create one new and check whether you are consistent now */
        outer@ for( i in 1..1 ) {
            for(clazz in clazzes) {
                val newEntity = clazz!!.constructors.first().call(asset, "mon_${count++}")
                if(stage.isConsistent(asset, listOf(newEntity), KB)) {
                    KB.addAssignedEntity(newEntity, asset)
                    break@outer
                }
            }
        }
        return emptyList()
    }
}

class HealthySemStage(override var count: Int) : HealthyStage(), SemanticStage{
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Entity> = abduct(asset, KB, this)
}

class SickSemStage(override var count: Int) : SickStage(), SemanticStage{
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Entity> = abduct(asset, KB, this)
}

val entityToClass = mapOf(Pair("ReqTenMonitor",  ReqTenMonitor::class),
                          Pair("ReqFiveMonitor", ReqFiveMonitor::class))