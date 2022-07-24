package server.forms.fields

abstract class FormField(
    val name: String, val value: String?, val attributes: MutableMap<String,String> = mutableMapOf(),
    val attributesWithoutValues: MutableList<String> = mutableListOf()) {
    abstract fun render() : String

    fun getLabel() : String = "<p class=\"form-label\">$name</p>"

    fun renderWithoutValue() : String{
        return render().replace("[a-zA-Z]+=\"[^\"]*\"".toRegex(),"")
    }

     fun apply(attr : String, value : String) : FormField{
         attributes[attr] = value
         return this
     }
     fun apply(attr : String, value : Boolean = true) : FormField{
        if(value) {
            attributesWithoutValues.add(attr)
        }
         return this
    }
    override fun toString(): String {
        return "type: ${this::class.java}, name: $name, attrs: $attributes , $attributesWithoutValues, value: $value"
    }
}