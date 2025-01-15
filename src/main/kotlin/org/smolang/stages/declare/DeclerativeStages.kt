package org.smolang.stages.declare

import org.smolang.stages.architecture.*
import org.smolang.stages.system.*


class HealthyDeclStage : HealthyStage() {
    private var count = 0
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Entity> {
        return listOf(ReqTenMonitor(asset, "rqTen_${count++}"))
    }
    override fun toString(): String = "Healthy"
}
class OkDeclStage : OkStage() {
    private var count = 0
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Entity> {
        return listOf(ReqWattOkMonitor(asset, "rqOk_${count++}"))
    }
    override fun toString(): String = "OK"
}

class MaintainDeclStage : MaintainStage() {
    private var count = 0
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Entity> {
        return listOf(ReqWattOkMonitor(asset, "rqOk_${count++}"))
    }
    override fun toString(): String = "Maintain"
}

class SickDeclStage : SickStage() {
    private var count = 0
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Monitor> {
        return listOf( ReqFiveMonitor(asset, "rqFive_${count++}"))//, null) //DefaultController("ctrl_${count++}"))
    }
    override fun toString(): String = "Sick"
}

