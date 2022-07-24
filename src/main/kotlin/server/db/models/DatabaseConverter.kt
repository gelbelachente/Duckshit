package server.db.models

import name
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.*

class DatabaseConverter(val model : KClass<out Model>){

    private fun resolveType(m : KType) : String{
        if(m.isSubtypeOf(String::class.createType())){
            return " varchar(10000)"
        }else
        if(m.isSubtypeOf(Int::class.createType())){
            return " integer"
        }else
        if (m.isSubtypeOf(Number::class.createType())){
         return " real"
        }else
            if(m.isSubtypeOf(Boolean::class.createType())){
                return " boolean"
            }

        error("${m.toString()} is not convertable by default to sql-data-type!")
    }

    fun isForeignKey(type : KType) : Boolean{
        return type.isSubtypeOf(Model::class.createType())
    }
    val foreign_keys = mutableListOf<String>()
    fun columnData() : String{
        var result = "("
        val c = model.primaryConstructor
        val sup = model.superclasses.first().primaryConstructor!!.parameters
        val params = c!!.parameters + sup
        for(param in params){
         if(param.annotations.any { it.annotationClass == Sql::class }){
             val sql = param.findAnnotations(Sql::class).first() as Sql
             if(isForeignKey(param.type)){
                 val c = (param.type.classifier as KClass<out Model>)
                 result += sql.name + " integer,"
                 foreign_keys.add("FOREIGN KEY(${sql.name}) REFERENCES ${c.simpleName}" +
                         " (${(c.superclasses.first().primaryConstructor!!.parameters.first().findAnnotations(Sql::class).first() as Sql).name}),")
             continue
             }

             result += sql.name + resolveType(param.type) +
                     (if(sql.pk) " PRIMARY KEY" else "") + (if (sql.auto) " AUTOINCREMENT" else "") + ","
         }
        }
        result += foreign_keys.firstOrNull() ?: ""
        return "${result.dropLast(1)})"
    }

    fun createData(update : Boolean = false) : String{
        return "CREATE TABLE" + (if(!update) " IF NOT EXISTS " else " " )+ "${model.java.simpleName}\n" + columnData()
    }


}