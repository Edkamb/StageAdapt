package org.smolang.setup

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.smolang.architecture.*
import org.smolang.declare.*
import kotlin.system.exitProcess

class Main : CliktCommand() {

    private val verbose by option("--verbose", "-v", help = "Verbose output.").flag()
    private val scenario by option().switch(
        "--test" to "test", "-t" to "test",
        "--scenario1" to "scen1", "-s1" to "scen1",
    ).default("test")

    override fun run() {
        org.apache.jena.query.ARQ.init()

        val sb = SemanticKnowledgeBase()
        val ast = Asset("ast1","kind1")
        sb.getKindedAssets("kind1")
        sb.addAsset(ast)
        sb.getKindedAssets("kind1")
        sb.removeAsset(ast)
        sb.getKindedAssets("kind1")
        sb.replace(ast, "prop", 1.0)
        var vv = sb.getValue(ast, "prop")
        println(vv)
        sb.replace(ast, "prop", 2.0)
        vv = sb.getValue(ast, "prop")
        println(vv)
        exitProcess(0)
        if(scenario == "test") {

            val sys = System(DeclareKnowledgeBase())
            val tagger = sys.tagger

            val ast1 = NVDIAsset("ast1", tagger, Basil)
            val ast2 = NVDIAsset("ast2", tagger, Basil)
            tagger.addAsset(ast1)
            tagger.addAsset(ast2)

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
                launch { ast1.repeatPush(0.2, 10.0, 1000, 5) }
                launch { ast2.repeatPush(0.7, 10.0, 1000, 5) }
                launch {
                    while (i-- > 0) {
                        delay(1000); sys.monitorCycle(); }
                }
                launch { delay(3000); sys.stageCycle() }
                if(verbose){launch {
                        while (j-- > 0) {
                            delay(1000); sys.print()
                        }
                    }
                }
                
            }
        }


    }
}

fun main(args:Array<String>) = Main().main(args)