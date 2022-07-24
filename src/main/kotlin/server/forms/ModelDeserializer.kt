package server.forms

import server.db.models.Model
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ModelDeserializer(val model : KClass<out Model>){
    fun deserialize(params : Map<String,Any>) : Model {
        val c =  model.primaryConstructor
        return c!!.callBy(params.mapKeys { c.parameters.find {p -> p.name == it.key }!! })
    }
}