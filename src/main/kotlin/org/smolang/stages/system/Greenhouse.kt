package org.smolang.stages.system

import kotlinx.coroutines.delay
import org.smolang.stages.architecture.*

const val Basil  = "Basil"

class NVDIAsset(name : String,
                private val ports : Ports,
                kind: String) : Asset(name, kind) {

    fun push(nvdi : Double, moisture : Double){
        ports.sensorUpdate(this,"n", nvdi)
        ports.sensorUpdate(this,"m", moisture)
    }

    suspend fun repeatPush(nvdi : Double, moisture : Double, off: Long, nr : Int){
        var c = nr
        while(c-- > 0) {
            delay(off)
            this.push(nvdi, moisture+ Math.random()*0.001-0.0005)
        }
    }
}

class ReqTenMonitor(val assetName : Asset, monName : String) : Monitor(monName, "Req10Mon") {
    override fun check(): Boolean= last["m"] == null || last["m"]!! >= 10
    override fun getPort(): String = "m"
    override fun toString(): String = "[Monitor >= 10 (${getName()}) for $assetName]"
}
class ReqFiveMonitor(val assetName : Asset, monName : String) : Monitor(monName, "Req10Mon") {
    override fun check(): Boolean = last["m"] == null || last["m"]!! >= 5
    override fun getPort(): String = "m"
    override fun toString(): String = "[Monitor >= 5 (${getName()}) for $assetName]"
}

abstract class HealthyStage : Stage {
    override fun isMember(candidate: Asset, KB: KnowledgeBase): Boolean {
        return KB.getValue(candidate, "n") >= 0.5
    }

    override fun isConsistent(name: Asset, mons: List<Entity>, KB: KnowledgeBase): Boolean {
        return mons.isNotEmpty() && mons.all { it is ReqTenMonitor }
    }

    override fun getKind(): String = Basil
}


abstract class SickStage : Stage {
    override fun isMember(candidate: Asset, KB: KnowledgeBase): Boolean {
        return KB.getValue(candidate, "n") < 0.5
    }

    override fun isConsistent(name : Asset, mons : List<Entity>, KB : KnowledgeBase) : Boolean {
        return mons.isNotEmpty() && mons.all { it is ReqFiveMonitor }
    }

    override fun getKind(): String = Basil
}