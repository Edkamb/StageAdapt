package org.smolang.architecture

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Node_URI
import org.apache.jena.rdf.model.Resource

/** Common structures for stages and the system architecture prototype **/

/*
  As we operate not on pointers, but on the identifiers of assets and system components,
  we have a common superclass for things with names/ids/URIs
*/
interface Nameable {
    val nName : String
    fun getName(): String = nName
}

/*
  We group assets into kinds (pumps, plants etc.), and to explicitly access them,
  they are modeled as an interface.
*/
interface Kindable {
    val nKind : String
    fun getKind(): String = nKind
}

/* An entity is anything that get messages from the tagger, so mostly monitors and controllers. */
open class Entity(override val nName: String,
                  override val nKind: String) : Nameable, Kindable {
    protected val last = mutableMapOf<String, Double>()
    fun setInput(port: String, newInput : Double) { last[port] = newInput }
    override fun toString(): String = nName

    val uri: Node = NodeFactory.createURI("http://www.smolang.org/stages#$nName")
    val uriKind: Node = NodeFactory.createURI("http://www.smolang.org/stages#$nKind")
    init {
        Common.entityUriMap[uri.uri] = this
    }
}

/* external assets. These must _NOT_ be referred from in the system, just with their names/ports */
open class Asset(final override val nName: String,
                 final override val nKind: String ) : Nameable, Kindable {
    override fun toString(): String = nName
    val uri: Node = NodeFactory.createURI("http://www.smolang.org/stages#$nName")
    val uriKind: Node = NodeFactory.createURI("http://www.smolang.org/stages#$nKind")
    init {
        Common.assetUriMap[uri.uri] = this
    }
}

object Common {
    val assetUriMap = mutableMapOf<String,Asset>()
    val entityUriMap = mutableMapOf<String,Entity>()
}