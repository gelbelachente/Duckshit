package server.serializer

import get
import server.http.reponse.TextResponse

open class Serializer(val ser : List<Any>, val fields : List<String>, val many : Boolean = false){

    private fun resolveValue(o : Any) : String{
        return if(o is String){
            "\"${o.toString()}\""
        }else if(o is Iterable<*>) {
            "[" + o.joinToString(",") { resolveValue(it!!) } + "]"
        }else if(o is Map<*,*>){
            "{" + o.map { resolveValue(it.key!!) + ":" + resolveValue(it.value!!) }.joinToString(",") + "}"
        }else{
            o.toString()
        }
    }

   open fun resolve() : TextResponse {
         var content = "[\n"
         ser.forEach {
             content +="{" + resolveFields(it).map { "\"${it.key}\" : ${resolveValue(it.value)}" }.joinToString(",\n") + "},"
         }
            content = content.dropLast(1)
          content += "]"
         if(!many){
             content.substring(1 until content.length-1)
         }
         return TextResponse(content)
     }
    private fun resolveFields(ser : Any) : Map<String,Any>{
        return fields.associateWith { ser.get(it) }
    }
}