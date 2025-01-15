package org.smolang.stages.system

import kotlinx.coroutines.delay
import org.smolang.stages.architecture.*

const val Basil  = "Basil"
const val PumpKind  = "Pump"
const val OK  = 1.0
const val MAINTAIN  = 0.0

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

class Pump(name : String,
                private val ports : Ports,
                kind: String) : Asset(name, kind) {

    fun push(watt : Double, status : Double){
        ports.sensorUpdate(this,"watt", watt)
        ports.sensorUpdate(this,"status", status)
    }

    suspend fun repeatPush(watt : Double, status : Double, off: Long, nr : Int){
        var c = nr
        while(c-- > 0) {
            delay(off)
            this.push(watt, status)
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
class ReqWattOkMonitor(val assetName : Asset, monName : String) : Monitor(monName, "Req10Mon") {
    override fun check(): Boolean = last["watt"] == null || last["watt"]!! <= 200
    override fun getPort(): String = "watt"
    override fun toString(): String = "[Monitor <= 200 (${getName()}) for $assetName]"
}
class ReqWattMaintainMonitor(val assetName : Asset, monName : String) : Monitor(monName, "Req10Mon") {
    override fun check(): Boolean = last["watt"] == null || last["watt"]!! <= 100
    override fun getPort(): String = "watt"
    override fun toString(): String = "[Monitor <= 100 (${getName()}) for $assetName]"
}

abstract class HealthyStage : Stage {
    override fun isMember(candidate: Set<Asset>, KB: KnowledgeBase): Boolean {
        if(candidate.size >= 2 ) throw Exception("Invalid use of single asset stage")
        if(candidate.first().getKind() != Basil) return false
        return KB.getValue(candidate.first(), "n") >= 0.5
    }

    override fun isConsistent(mons: List<Entity>, KB: KnowledgeBase): Boolean {
        return mons.isNotEmpty() && mons.any { it is ReqTenMonitor }
    }

    override fun getKind(): String = Basil
}


abstract class SickStage : Stage {
    override fun isMember(candidate: Set<Asset>, KB: KnowledgeBase): Boolean {
        if(candidate.size >= 2 ) throw Exception("Invalid use of single asset stage")
        if(candidate.first().getKind() != Basil) return false
        return KB.getValue(candidate.first(), "n") < 0.5
    }

    override fun isConsistent(mons : List<Entity>, KB : KnowledgeBase) : Boolean {
        return mons.isNotEmpty() && mons.any { it is ReqFiveMonitor }
    }

    override fun getKind(): String = Basil
}


abstract class OkStage : Stage {
    override fun isMember(candidate: Set<Asset>, KB: KnowledgeBase): Boolean {
        if(candidate.size >= 2 ) throw Exception("Invalid use of single asset stage")
        if(candidate.first().getKind() != PumpKind) return false
        return KB.getValue(candidate.first(), "status") == OK //0.0 encodes OK
    }

    override fun isConsistent(mons : List<Entity>, KB : KnowledgeBase) : Boolean {
        return mons.isNotEmpty() && mons.any { it is ReqWattOkMonitor }
    }

    override fun getKind(): String = PumpKind
}
abstract class MaintainStage : Stage {
    override fun isMember(candidate: Set<Asset>, KB: KnowledgeBase): Boolean {
        if(candidate.size >= 2 ) throw Exception("Invalid use of single asset stage")
        if(candidate.first().getKind() != PumpKind) return false
        return KB.getValue(candidate.first(), "status") == MAINTAIN //0.0 encodes OK
    }

    override fun isConsistent(mons : List<Entity>, KB : KnowledgeBase) : Boolean {
        return mons.isNotEmpty() && mons.any { it is ReqWattMaintainMonitor }
    }

    override fun getKind(): String = PumpKind
}