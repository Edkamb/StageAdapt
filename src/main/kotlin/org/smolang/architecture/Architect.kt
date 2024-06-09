package org.smolang.architecture

interface Ports {
    fun sensorUpdate(asset: Asset, portName : String, value : Double)
    fun addAsset(name : String, kind : String)
    fun removeAsset(name : String, kind : String)
}

interface Knowledge
data class ExistsEntity(val entity: Entity) : Knowledge {
    override fun toString(): String {
        return "ExistsEntity($entity)"
    }
}
data class ExistsAsset(val asset: Asset) : Knowledge {
    override fun toString(): String {
        return "ExistsAsset($asset)"
    }
}
data class AssignedTo(val asset: Asset, val entity: Entity) : Knowledge {
    override fun toString(): String {
        return "AssignedTo($asset, $entity)"
    }
}
data class HasValue(val asset: Asset, val property : String, val value: Double) : Knowledge {
    override fun toString(): String {
        return "HasValue($asset, $property, $value)"
    }
}

class KnowledgeBase(val system: System) : Entity(""){
    private val knowledge = mutableSetOf<Knowledge>()
    fun print() : String{
        return knowledge.joinToString(", ")
    }
    fun add(k : Knowledge) { knowledge.add(k) }
    fun retract(k : Knowledge) { knowledge.remove(k) }

    fun getValue(asset: Asset, s: String): Double {
        val f = knowledge.filterIsInstance<HasValue>().firstOrNull { it.asset == asset && it.property == s }
        if(f == null) throw Exception("No value for ${asset.getName()} on property $s")
        return f.value
    }
    fun getKindedAssets(kind : String) : List<Asset>{
        return knowledge.filterIsInstance<ExistsAsset>().filter{it.asset.nKind == kind }.map { it.asset }
    }
    fun addAsset(name : String, kind: String){
        knowledge.add(ExistsAsset(Asset(name, kind)))
    }
    fun removeAsset(asset: Asset){
        knowledge.remove(ExistsAsset(asset))
    }

    fun replace(asset: Asset, portName : String, value : Double) {
        knowledge.removeAll(knowledge.filterIsInstance<HasValue>().filter { it.asset == asset && it.property == portName }
            .toSet())
        knowledge.add(HasValue(asset, portName, value))
        knowledge.filterIsInstance<AssignedTo>().filter { it.asset == asset }.map { it.entity }.forEach {it.setInput(portName, value)}

    }

    fun getAssigned(asset : Asset) : List<Entity>{
        return knowledge.filterIsInstance<AssignedTo>().filter{it.asset == asset }.map { it.entity }
    }
}


class Tagger(private val KB : KnowledgeBase) : Ports {

    override fun sensorUpdate(asset: Asset, portName : String, value : Double){
        KB.replace(asset, portName, value)
    }
    override fun addAsset(name : String, kind: String){
        KB.addAsset(name, kind)
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
        var toAdd = listOf<Pair<Asset, List<Entity>>>()
        for(stage in stages){
            val members = KB.getKindedAssets(stage.getKind()).filter { stage.isMember(it, KB) }
            for(member in members)
                if(!stage.isConsistent(member, KB.getAssigned(member), KB)) { // this computes V (l.5) M

                    println("   inconsistent: $member")
                    val ret = stage.gen(member)
                    toAdd = toAdd + Pair(member, ret)     // this computer toGenerate (l.9) A
                }
        }

        // repair / PE
        for( newPair in toAdd)
            for( mon in newPair.second)
                system.addAssignedEntity(mon, newPair.first)


    }
}

class System : Entity("System"){

    var mons : List<Monitor> = emptyList()

    val KB : KnowledgeBase = KnowledgeBase(this)
    val tagger  = Tagger(KB)
    val stages = StageMonitor(this,KB)

    fun print(){
        println(KB.print())
        //println("Controllers: "+ctrls.values.joinToString(", "))
        //println("Monitors: "+mons.values.joinToString(", "))
    }
    fun controlCycle(){
        //ctrls.values.forEach { it.control() }
    }

    fun stageCycle(){
        stages.detectMissing()
    }
    fun monitorCycle(){
        mons.forEach { if(!it.check()) println("monitor ${it.getName()} reports error!") }
    }

    fun addAssignedEntity(entity: Entity, assigned: Asset){
        KB.add(AssignedTo(assigned, entity))
        KB.add(ExistsEntity(entity))
        if(entity is Monitor) mons = mons + entity
    }


    fun addStage(hStage: Stage) {
        stages.addStage(hStage)
    }
}



