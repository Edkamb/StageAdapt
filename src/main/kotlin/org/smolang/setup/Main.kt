package org.smolang.setup

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.smolang.architecture.*
import org.smolang.declare.*

class Main : CliktCommand() {

    private val verbose by option("--verbose", "-v", help = "Verbose output.").flag()

    override fun run() {
        org.apache.jena.query.ARQ.init()
        println("Hello World")





        val sys = System()
        val tagger = sys.tagger

        val ast1 = NVDIAsset("ast1", tagger, Basil)
        val ast2 = NVDIAsset("ast2", tagger, Basil)
        tagger.addAsset(ast1.getKind(), ast1.getName())
        tagger.addAsset(ast2.getKind(), ast2.getName())

        val ctrl1 = DefaultController("ctrl1")
        val ctrl2 = DefaultController("ctrl2")
        sys.addAssignedEntity(ctrl1, ast1)
        sys.addAssignedEntity(ctrl2, ast2)

        val mon1 = ReqTenMonitor(ast1, "mon1")
        val mon2 = ReqTenMonitor(ast2, "mon2")
        sys.addAssignedEntity(mon1, ast1)
        sys.addAssignedEntity(mon2, ast2)

        sys.addStage(HealthyStage())
        sys.addStage(SickStage())

        runBlocking {
            var i = 10
            var j = 10
            launch { ast1.repeatPush(0.2, 10.0, 1000,5) }
            launch { ast2.repeatPush(0.7, 10.0, 1000,5) }
            launch { while(i-- > 0) {delay(1000); sys.controlCycle(); sys.monitorCycle(); }}
            launch { delay(3000); sys.stageCycle() }
            launch { while(j-- > 0) { delay(1000); sys.print()} }
        }


    }
}

fun main(args:Array<String>) = Main().main(args)