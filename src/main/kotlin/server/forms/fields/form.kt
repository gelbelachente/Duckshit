package server.forms.fields

import server.db.models.Model


class form {


    class IntField(name : String, value : String?) : FormField(name, value){
        override fun render(): String {
            value?.toIntOrNull() ?: if(value != null) error("$value can't be converted to Integer") else ""

            return getLabel() + "<input name=\"$name\" class=\"form-field\" type=\"number\" value=\"${value ?: ""}\" placeholder=\"$name\" step=\"1\"" +
                    attributes.map { it.key + "=\"" + it.value +"\"" }.joinToString(" ") + " " +
                    attributesWithoutValues.joinToString(" ") +
                    " />"        }

    }

    class FloatField(name : String, value : String?) : FormField(name,value) {
        override fun render(): String {
            value?.toFloatOrNull() ?: if(value != null) error("$value can't be converted to Float") else ""

            return getLabel() + "<input name=\"$name\" class=\"form-field\" type=\"number\" value=\"${value ?: ""}\" placeholder=\"$name\" " +
                    attributes.map { it.key + "=\"" + it.value +"\"" }.joinToString(" ") + " " +
                    attributesWithoutValues.joinToString(" ") +
                    " />"        }
    }

    class ChoiceField(name : String,val selected : Any?, val choices : List<Any>) : FormField(name,selected.toString()){
        override fun render(): String {
            return getLabel() +
                    "<select name=\"$name\" class=\"form-field\"" +
                    attributes.map { it.key + "=\"" + it.value +"\"" }.joinToString(" ") + " " +
                    attributesWithoutValues.joinToString(" ") + ">" +
                    choices.joinToString(""){ getChoiceField(it) } +
                    "</select>"
        }
        private fun getChoiceField(v : Any) : String{
            return if(v !is Model){
                "<option" + (if(selected == v) " selected " else " ") + "name=\"${v.toString()}\">${v.toString()}</option>"
            }else{
                "<option" + (if((selected as Model).id == v.id) " selected " else " ") + "name=\"${v.id}\">${v.toString()}</option>"
            }

        }

    }

    class CharField(name : String, value : String?) : FormField(name,value){
        override fun render(): String {
            return getLabel() + " <input class=\"form-field\"  name=\"$name\" value=\"${value ?: ""}\" placeholder=\"$name\" type=\"text\" " +
                    attributes.map { it.key + "=\"" + it.value +"\"" }.joinToString(" ") + " " +
                    attributesWithoutValues.joinToString(" ") +
                     " />"
        }

    }


}