package org.smolang.stages.architecture


interface Ports {
    fun sensorUpdate(asset: Asset, portName : String, value : Double)
    fun addAsset(asset: Asset)
    fun removeAsset(name : String, kind : String)
}


class Tagger(private val KB : KnowledgeBase) : Ports {

    override fun sensorUpdate(asset: Asset, portName : String, value : Double){
        KB.replace(asset, portName, value)
    }
    override fun addAsset(asset: Asset){
        KB.addAsset(asset)
    }
    override fun removeAsset(name : String, kind: String){
        KB.removeAsset(Asset(name, kind))
    }

}



/**
 Our self-adaptation
 */
class StageMonitor(val system: System, val KB: KnowledgeBase) {
    //stages
    val stages = mutableSetOf<Stage>()

    fun addStage(stage : Stage){ stages.add(stage) }


    fun detectMissing(){
        println("start detection")
        var toAdd = listOf<Pair<Asset, List<Entity>>>()
        for(stage in stages){
            val kinded = KB.getKindedAssets(stage.getKind())
            val members = kinded.filter { if(it is Aggregate) stage.isMember(KB.getParts(it, KB).toSet(), KB) else stage.isMember(it, KB) }
            for(member in members) {
                val assigned = KB.getAssigned(member)
                if (!stage.isConsistent(assigned, KB)) { // this computes V (l.5) M
                    println("   inconsistent: $member with $stage")
                    val ret = stage.gen(member, KB)
                    toAdd = toAdd + Pair(member, ret)     // this computes toGenerate (l.9) A
                }
            }
        }

        // repair / PE
        for( newPair in toAdd)
            for( mon in newPair.second)
                system.addAssignedEntity(mon, newPair.first)


    }
}

class System(val KB : KnowledgeBase) {

    var mons : List<Monitor> = emptyList()

    val tagger  = Tagger(KB)
    val stages = StageMonitor(this,KB)

    fun print(){
        println(KB.print())
    }
    fun stageCycle(){
        stages.detectMissing()
    }
    fun monitorCycle(){
        mons.forEach { if(!it.check()) println("monitor ${it.getName()} reports error!") }
    }

    fun addAssignedEntity(entity: Entity, assigned: Asset){
        KB.addAssignedEntity(entity,assigned)
        if(entity is Monitor) mons = mons + entity
    }


    fun addStage(hStage: Stage) {
        stages.addStage(hStage)
    }
}



