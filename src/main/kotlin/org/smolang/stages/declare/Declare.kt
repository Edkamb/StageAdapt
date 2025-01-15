package org.smolang.stages.declare

import org.smolang.stages.architecture.Aggregate
import org.smolang.stages.architecture.Asset
import org.smolang.stages.architecture.Entity
import org.smolang.stages.architecture.KnowledgeBase


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
data class HasPart(val aggregate: Aggregate, val asset: Asset) : Knowledge

class DeclareKnowledgeBase : KnowledgeBase(){
    private val knowledge = mutableSetOf<Knowledge>()
    private var count = 0
    override fun print() : String{
        return knowledge.joinToString(", ")
    }
    fun add(k : Knowledge) { knowledge.add(k) }


    override fun aggregate(assets: List<Asset>) {

        val myAst = assets.map { it.nKind }.joinToString(".")
        val aggregate = Aggregate("aggr"+(count++), myAst)
        assets.forEach { knowledge.add(HasPart(aggregate, it)) }
        knowledge.add(ExistsAsset(aggregate))
    }

    override fun getParts(aggr: Aggregate, kb: KnowledgeBase): List<Asset> {
        return knowledge.filterIsInstance<HasPart>().filter { it.aggregate == aggr }.map { it.asset }
    }

    override fun addAssignedEntity(entity: Entity, assigned: Asset) {
        knowledge.add(AssignedTo(assigned, entity))
        knowledge.add(ExistsEntity(entity))
    }

    override fun removeEntity(e: Entity) {
        TODO("Not yet implemented, not needed for declarative stages ")
    }

    override fun getPossibleEntities(): List<String> {
        TODO("Not yet implemented, not needed for declarative stages")
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
        if(asset is Aggregate)
            return knowledge.filterIsInstance<HasPart>()
                            .filter { it.aggregate == asset }
                            .map { it.asset }
                            .fold(listOf()) {acc, nx -> acc + getAssigned(nx)}
        return knowledge.filterIsInstance<AssignedTo>().filter{it.asset == asset }.map { it.entity }
    }

}