package server.forms

import server.db.DuckshitDatabase
import server.db.Search
import server.db.models.Model
import kotlin.reflect.KClass


class ModelForm(val model: KClass<out Model>, val displayFields : List<String>, val id : Int = -1, val empty : Boolean = false) :
    Form(ModelFormConverter(model,displayFields,DuckshitDatabase.instance.getOrNull(model, Search("id = $id")),empty).resolveFields())


