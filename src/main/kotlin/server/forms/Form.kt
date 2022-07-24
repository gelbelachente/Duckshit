package server.forms

import server.forms.fields.FormField

open class Form(val fields : List<FormField>) {

    private val methods = listOf("GET","POST","PUT","DELETE")

    fun get(name : String) : FormField{
        return fields.find { it.name == name } ?: error("Formfield $name could not be found!")
    }

    fun render(method : String = "POST", action : String = "") : String{
        if(method !in methods) error("$method is no valid method!")
       return  "<form class=\"model-form\" method=\"$method\" action=\"$action\">\n"+
               fields.joinToString(" <br> "){it.render() + "\n"} +
               "<br> <input value=\"submit\" type=\"submit\" class=\"form-submit\"> " +
               "</form>"
    }

    fun renderWithoutValue(method : String = "POST", action : String = "") : String{
        if(method !in methods) error("$method is no valid method!")
        return  "<form class=\"model-form\" method=\"$method\" action=\"$action\">\n"+
                fields.joinToString(" <br> "){it.renderWithoutValue() + "\n"} +
                "<br> <input type=\"submit\" class=\"form-submit\"> " +
                "</form>"
    }

    val render : String get() = render()

    override fun toString(): String {
        return fields.joinToString(", ", "[", "]") { it.toString() }
    }
}

