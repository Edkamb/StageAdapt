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

