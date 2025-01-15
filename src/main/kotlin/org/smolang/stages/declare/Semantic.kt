package org.smolang.stages.declare

import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.ReasonerRegistry
import org.apache.jena.update.UpdateAction
import org.apache.jena.update.UpdateFactory
import org.smolang.stages.architecture.*
import java.io.ByteArrayInputStream
import java.io.File

class SemanticKnowledgeBase : KnowledgeBase(){
 private var count = 0
  private val model : Model //= ModelFactory.createDefaultModel()
   init {
       val f = ModelFactory.createDefaultModel().read(ByteArrayInputStream(File("coreOnto.ttl").readText().toByteArray()), null, "TTL")
       model = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), f)

   }
    override fun print(): String {
        return model.toString()
    }

    fun getFirstValueOrNull(asset: Asset, portName: String): Double {
        val queryWithPrefixes = """
             SELECT ?a {<${asset.uri.uri}> <http://www.smolang.org/stages#$portName> ?a}
        """.trimIndent()

        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        val ret = res.next().getLiteral("?a").double
        return ret
    }

    override fun getValue(asset: Asset, portName: String): Double {
        val queryWithPrefixes = """
             SELECT ?a {<${asset.uri.uri}> <http://www.smolang.org/stages#$portName> ?a}
        """.trimIndent()

        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        if(!res.hasNext()) throw Exception("failure to retrieve data")
        val ret = res.next().getLiteral("?a").double
        if(res.hasNext()) throw Exception("malformed KG")
        return ret
    }

    override fun getKindedAssets(kind: String): List<Asset> {
        if(kind.contains(".")){
            val kinds = kind.split(".")
            val qStr = kinds.map { " <http://www.smolang.org/stages#hasPart> [ a <http://www.smolang.org/stages#$it> ]" }.joinToString(";")
            val queryWithPrefixes = """
            SELECT ?ast { ?ast a <http://www.smolang.org/stages#Aggregate>; $qStr .} 
        """.trimIndent()
            val query = QueryFactory.create(queryWithPrefixes)
            val qexec = QueryExecutionFactory.create(query, model)
            val res = qexec.execSelect()
            var list = listOf<Asset?>()
            res.forEach { val f = Common.assetUriMap[it.get("?ast").toString()]; list = list + f}
            return list.filterNotNull()
        }
        val queryWithPrefixes = """
            SELECT ?ast { ?ast a <http://www.smolang.org/stages#$kind> } 
        """.trimIndent()
        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        var list = listOf<Asset?>()
        res.forEach { val f = Common.assetUriMap[it.get("?ast").toString()]; list = list + f}
        //println(res.hasNext())
        return list.filterNotNull()
    }

    override fun addAsset(asset: Asset) {
        val queryWithPrefixes = """
            INSERT DATA { <${asset.uri.uri}> a <${asset.uriKind}> } 
        """.trimIndent()
        val query = UpdateFactory.create(queryWithPrefixes)
        UpdateAction.execute(query, model)
    }

    override fun removeAsset(asset: Asset) {
        val queryWithPrefixes = """
            DELETE DATA { <${asset.uri.uri}> a <${asset.uriKind}> } 
        """.trimIndent()

        val query = UpdateFactory.create(queryWithPrefixes)
        UpdateAction.execute(query, model)
    }

    override fun replace(asset: Asset, portName: String, value: Double) {

        val queryWithPrefixes = """
            DELETE WHERE { <${asset.uri.uri}> <http://www.smolang.org/stages#$portName> ?a };
            INSERT DATA { <${asset.uri.uri}> <http://www.smolang.org/stages#$portName> $value }
        """.trimIndent()

        val query = UpdateFactory.create(queryWithPrefixes)
        UpdateAction.execute(query, model)
    }

    /*fun printAllAssigned(){

        val queryWithPrefixes = """
            SELECT DISTINCT ?ast ?ent { ?ast <http://www.smolang.org/stages#assignedTo> ?ent } 
        """.trimIndent()
        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        var listA = listOf<Asset?>()
        var list = listOf<Entity?>()
        res.forEach { val f = Common.entityUriMap[it.get("?ent").toString()]; list = list + f}
        res.forEach { val f = Common.assetUriMap[it.get("?ent").toString()]; listA = listA + f}
        println(list)
        println(listA)
    }*/

    override fun getAssigned(asset: Asset): List<Entity> {
        if(asset is Aggregate){
            //printAllAssigned()
            val queryWithPrefixes = """
            SELECT DISTINCT ?ent { <${asset.uri.uri}> a <http://www.smolang.org/stages#Aggregate>; 
                                                      <http://www.smolang.org/stages#hasPart> [<http://www.smolang.org/stages#assignedTo> ?ent] } 
        """.trimIndent()
            val query = QueryFactory.create(queryWithPrefixes)
            val qexec = QueryExecutionFactory.create(query, model)
            val res = qexec.execSelect()
            var list = listOf<Entity?>()
            res.forEach { val f = Common.entityUriMap[it.get("?ent").toString()]; list = list + f}
            return list.filterNotNull()
        }
        val queryWithPrefixes = """
            SELECT ?ent { <${asset.uri.uri}> <http://www.smolang.org/stages#assignedTo> ?ent } 
        """.trimIndent()
        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        var list = listOf<Entity?>()
        res.forEach { val f = Common.entityUriMap[it.toString()]; list = list + f}
        //println(res.hasNext())
        return list.filterNotNull()
    }

    override fun addAssignedEntity(entity: Entity, assigned: Asset) {
        val queryWithPrefixes = """
            INSERT DATA { <${entity.uri.uri}> a <${entity.uriKind}>.
                          <${assigned.uri.uri}> <http://www.smolang.org/stages#assignedTo> <${entity.uri.uri}>.  } 
        """.trimIndent()
        //println(queryWithPrefixes)
        val query = UpdateFactory.create(queryWithPrefixes)
        UpdateAction.execute(query, model)
    }


    override fun removeEntity(e: Entity) {
        val queryWithPrefixes = """
            DELETE DATA { <${e.uri.uri}> a <${e.uriKind}> } 
        """.trimIndent()

        val query = UpdateFactory.create(queryWithPrefixes)
        UpdateAction.execute(query, model)
    }

    override fun getPossibleEntities(): List<String> {
        val queryWithPrefixes = """
            SELECT ?ent { ?ent <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://www.smolang.org/stages#Component> } 
        """.trimIndent()
        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        var list = listOf<String?>()
        res.forEach { list = list + it.toString()}
        return list.filterNotNull()
    }

    override fun aggregate(assets: List<Asset>) {
        val aggr = Aggregate("aggr${count++}","Aggregate")
        addAsset(aggr)
        assets.map { "<${aggr.uri.uri}> <http://www.smolang.org/stages#hasPart> <${it.uri.uri}>." }.joinToString("\n")
        val queryWithPrefixes = """
            INSERT DATA { ${assets.map { "<${aggr.uri.uri}> <http://www.smolang.org/stages#hasPart> <${it.uri.uri}>." }.joinToString("\n")}  } 
        """.trimIndent()
        //println(queryWithPrefixes)
        val query = UpdateFactory.create(queryWithPrefixes)
        UpdateAction.execute(query, model)
    }

    override fun getParts(aggr: Aggregate, kb: KnowledgeBase): List<Asset> {
        val queryWithPrefixes = """
            SELECT DISTINCT ?part { <${aggr.uri.uri}> a <http://www.smolang.org/stages#Aggregate>; 
                                                      <http://www.smolang.org/stages#hasPart> ?part } 
        """.trimIndent()
        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        var list = listOf<Asset?>()
        res.forEach { val f = Common.assetUriMap[it.get("?part").toString()]; list = list + f}
        return list.filterNotNull()
    }
}