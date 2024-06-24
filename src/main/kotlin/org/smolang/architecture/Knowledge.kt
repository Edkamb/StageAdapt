package org.smolang.architecture

abstract class KnowledgeBase {
    abstract fun print() : String

    abstract fun getValue(asset: Asset, s: String): Double
    abstract fun getKindedAssets(kind : String) : List<Asset>
    abstract fun addAsset(asset: Asset)
    abstract fun removeAsset(asset: Asset)
    abstract fun replace(asset: Asset, portName : String, value : Double)
    abstract fun getAssigned(asset : Asset) : List<Entity>
    abstract fun addAssignedEntity(entity: Entity, assigned: Asset)
}

class DeclareKnowledgeBase : KnowledgeBase(){
    private val knowledge = mutableSetOf<Knowledge>()
    override fun print() : String{
        return knowledge.joinToString(", ")
    }
    fun add(k : Knowledge) { knowledge.add(k) }


    override fun addAssignedEntity(entity: Entity, assigned: Asset) {
        knowledge.add(AssignedTo(assigned, entity))
        knowledge.add(ExistsEntity(entity))
    }

    override fun getValue(asset: Asset, s: String): Double {
        val f = knowledge.filterIsInstance<HasValue>().firstOrNull { it.asset == asset && it.property == s }
        if(f == null) throw Exception("No value for ${asset.getName()} on property $s")
        return f.value
    }
    override fun getKindedAssets(kind : String) : List<Asset>{
        val ex = knowledge.filterIsInstance<ExistsAsset>()
        return ex.filter{it.asset.nKind == kind }.map { it.asset }
    }
    override fun addAsset(asset: Asset){
        knowledge.add(ExistsAsset(asset))
    }
    override fun removeAsset(asset: Asset){
        knowledge.remove(ExistsAsset(asset))
    }

    override fun replace(asset: Asset, portName : String, value : Double) {
        knowledge.removeAll(knowledge.filterIsInstance<HasValue>().filter { it.asset == asset && it.property == portName }
            .toSet())
        knowledge.add(HasValue(asset, portName, value))
        knowledge.filterIsInstance<AssignedTo>().filter { it.asset == asset }.map { it.entity }.forEach {it.setInput(portName, value)}

    }

    override fun getAssigned(asset : Asset) : List<Entity>{
        return knowledge.filterIsInstance<AssignedTo>().filter{it.asset == asset }.map { it.entity }
    }

}