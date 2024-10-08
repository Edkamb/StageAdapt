package org.smolang.stages.setup

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.smolang.stages.architecture.Asset
import org.smolang.stages.architecture.DefaultController
import org.smolang.stages.architecture.System
import org.smolang.stages.declare.*
import org.smolang.stages.system.Basil
import org.smolang.stages.system.NVDIAsset
import org.smolang.stages.system.ReqTenMonitor
import kotlin.random.Random
import kotlin.system.exitProcess

class Main : CliktCommand() {

    private val verbose by option("--verbose", "-v", help = "Verbose output").flag()
    private val scenario by option().switch(
        "--test" to "test", "-t" to "test",
        "--scenario1" to "scen1", "-s1" to "scen1",
        "--scenario2" to "scen2", "-s2" to "scen2",
        "--generic_declare" to "genD", "-gD" to "genD",
        "--generic_semantic" to "genS", "-gS" to "genS",
    ).default("test")
    private val nrStage by option("-m", help = "number stages").int().default(2)
    private val nrAssets by option("-n", help = "number assets").int().default(2)

    override fun run() {
        org.apache.jena.query.ARQ.init()

        if(scenario == "genD") {
            val sys = System(DeclareKnowledgeBase())
            val tagger = sys.tagger

            val stages = mutableListOf<GenericDeclareNVDIStage>()
            for (i in 0 ..< nrStage)
                stages.add(GenericDeclareNVDIStage(i.toDouble()..i.toDouble()*10, i))

            val assets = mutableListOf<NVDIAsset>()
            for (i in 0 ..< nrAssets)
                assets.add(NVDIAsset("ast$i",tagger, Basil))

            assets.forEach { tagger.addAsset(it) }
            stages.forEach { sys.addStage(it) }


            assets.forEach { it.push(Random.nextDouble(0.1, nrStage.toDouble()*10), 0.0) }

            val start = java.lang.System.currentTimeMillis()
            sys.stageCycle()
            val end = java.lang.System.currentTimeMillis()
            println("$nrStage,$nrAssets,${(end-start)}")

            exitProcess(0)
        }
        if(scenario == "genS") {
            val sys = System(SemanticKnowledgeBase())
            val tagger = sys.tagger

            val stages = mutableListOf<GenericSemanticNVDIStage>()
            for (i in 0 ..< nrStage)
                stages.add(GenericSemanticNVDIStage(0,i.toDouble()..i.toDouble()*10, i))

            val assets = mutableListOf<NVDIAsset>()
            for (i in 0 ..< nrAssets)
                assets.add(NVDIAsset("ast$i",tagger, Basil))

            assets.forEach { tagger.addAsset(it) }
            stages.forEach { sys.addStage(it) }


            assets.forEach { it.push(Random.nextDouble(0.1, nrStage.toDouble()*10), 0.0) }

            val start = java.lang.System.currentTimeMillis()
            sys.stageCycle()
            val end = java.lang.System.currentTimeMillis()
            sys.stageCycle()
            println("$nrStage,$nrAssets,${(end-start)}")

            exitProcess(0)
        }
        if(scenario == "test") {
            val sb = SemanticKnowledgeBase()
            val ast = Asset("ast1", "kind1")
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
        }

        val sys = if(scenario == "scen1") System(DeclareKnowledgeBase()) else System(SemanticKnowledgeBase())
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

        sys.addStage(if(scenario == "scen1")HealthyDeclStage() else HealthySemStage(0))
        sys.addStage(if(scenario == "scen1")SickDeclStage() else SickSemStage(0))

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

fun main(args:Array<String>) = Main().main(args)