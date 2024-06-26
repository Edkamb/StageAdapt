package org.smolang.architecture

import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.ReasonerRegistry
import org.apache.jena.update.UpdateAction
import org.apache.jena.update.UpdateFactory
import java.io.ByteArrayInputStream
import java.io.File

class SemanticKnowledgeBase : KnowledgeBase(){

  private val model : Model //= ModelFactory.createDefaultModel()
   init {
       val f = ModelFactory.createDefaultModel().read(ByteArrayInputStream(File("coreOnto.ttl").readText().toByteArray()), null, "TTL")
       model = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), f)

       println(model.numPrefixes())
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

        val queryWithPrefixes = """
            SELECT ?ast { ?ast a <http://www.smolang.org/stages#$kind> } 
        """.trimIndent()
        val query = QueryFactory.create(queryWithPrefixes)
        val qexec = QueryExecutionFactory.create(query, model)
        val res = qexec.execSelect()
        var list = listOf<Asset?>()
        res.forEach { val f = Common.assetUriMap[it.toString()]; list = list + f}
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

    override fun getAssigned(asset: Asset): List<Entity> {
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
        val query = UpdateFactory.create(queryWithPrefixes)
        UpdateAction.execute(query, model)
    }
}