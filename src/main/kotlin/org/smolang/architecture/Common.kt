package org.smolang.architecture

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
open class Entity(eName : String) : Nameable {
    protected val last = mutableMapOf<String, Double>()
    override val nName: String = eName
    fun setInput(port: String, newInput : Double) { last[port] = newInput }
    override fun toString(): String {
        return nName
    }
}

/* external assets. These must _NOT_ be referred from in the system, just with their names/ports */
open class Asset(aName : String, aKind : String) : Nameable, Kindable {
    override val nKind: String = aKind
    override val nName: String = aName
    override fun toString(): String {
        return nName
    }
}