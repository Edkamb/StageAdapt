package org.smolang.stages.declare

import org.smolang.stages.architecture.*
import org.smolang.stages.system.*


class HealthyDeclStage : HealthyStage() {
    private var count = 0
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Entity> {
        return listOf(ReqTenMonitor(asset, "rqTen_${count++}"))
    }
}

class SickDeclStage : SickStage() {
    private var count = 0
    override fun gen(asset: Asset, KB : KnowledgeBase): List<Monitor> {
        return listOf( ReqFiveMonitor(asset, "rqFive_${count++}"))//, null) //DefaultController("ctrl_${count++}"))
    }
}

