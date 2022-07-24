package server.serializer

import get
import server.db.models.Model
import server.db.models.Sql
import server.http.reponse.TextResponse
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

class ModelSerializer (val m : List<Model>, val fields : List<String>, val many : Boolean = false) {

     fun resolve(): TextResponse {
        var content = "[\n"
        m.forEach {
            content +="{" + resolveFields(it).map { "\"${it.key}\" : ${resolveValue(it.value)}" }.joinToString(",\n") + "},"
        }
        content = content.dropLast(1)
        content += "]"
        if(!many){
            content = content.substring(1 until content.length-1)
        }
        return TextResponse(content)
    }

    private fun resolveValue(o : Any?) : String{
        return if(o == null) {
            "null"
        }else if(o is String){
            "\"${o.toString()}\""
        }else if(o is Iterable<*>) {
            "[" + o.joinToString(",") { resolveValue(it!!) } + "]"
        }else if(o is Map<*,*>){
            "{" + o.map { resolveValue(it.key!!) + ":" + resolveValue(it.value!!) }.joinToString(",") + "}"
        }else{
            o.toString()
        }
    }

    private fun resolveFields(m : Model) : Map<String,Any?>{
        val fields = m::class.members.filter {
            m::class.primaryConstructor!!.parameters.find {p-> it.name == p.name }?.annotations?.any { it.annotationClass == Sql::class  } == true
        }.filter { (fields.isNotEmpty() && fields.first() == "*") || fields.contains(it.name) }

        return fields.associate { it.name to (it as KProperty1<Any, *>).get(m) }
    }

}