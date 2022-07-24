package server.forms

import convert
import get
import server.db.DuckshitDatabase
import server.db.models.Model
import server.db.models.Sql
import server.forms.fields.FormField
import server.forms.fields.form
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class ModelFormConverter(val model : KClass<out Model>, val displayFields: List<String>, val instance : Model? = null, val empty : Boolean = false){

     fun resolveFields() : List<FormField>{
        val result = mutableListOf<FormField>()
        val c = model.primaryConstructor!!
        for(param in c.parameters){
            if(param.annotations.any { it.annotationClass == Sql::class }){
                val sql = param.findAnnotations(Sql::class).first()
                val type = resolveType(sql.name,param.type.classifier as KClass<*>, instance?.get(param.name!!),param.isOptional)
                result.add(type)
            }
        }
        return result.filter { it.name in displayFields || (displayFields.size == 1 && displayFields.first().trim() == "*")}
     }

    private fun resolveType( name : String, type : KClass<*>, v : Any?, isOptional : Boolean) : FormField {
        val value = if(empty) null else v

        return  ({
            if (type.java.isAssignableFrom(String::class.java)) {
                 form.CharField(name, value?.convert()?.removeSurrounding("'"))
            } else if (type.java.isAssignableFrom(Int::class.java)) {
                 form.IntField(name, value?.convert())
            } else if (type.isSubclassOf(Model::class)) {
                 form.ChoiceField(name, value, DuckshitDatabase.instance.all(type as KClass<out Model>))
            } else if (type.java.isAssignableFrom(Boolean::class.java)) {
                 form.ChoiceField(name, value?.convert(), listOf("true", "false"))
            } else if (type.java.isAssignableFrom(Number::class.java)) {
                 form.FloatField(name, value?.convert())
            }

            error("${model.java.canonicalName} is not convertable by default to html-data-type!")
        } as FormField).apply {
            if(!isOptional) apply("required")
        }

        }

}