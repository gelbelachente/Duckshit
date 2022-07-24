package server.template

import getIndex

data class Container(val type : Type, val body : String)

class Lexer(val text : String) {

    fun getType(str : String) : Type {
        return if(str.startsWith("%%if") && str.endsWith("%%")){
            Type.CONDITION_START
        }else if(str.equals("%%endif%%")){
            Type.CONDITION_END
        }else if(str.equals("%%else%%")){
            Type.CONDITION_ELSE
        }else if(str.startsWith("%%for") && str.endsWith("%%") && str.contains("in")){
            Type.FOR_START
        }else if(str == "%%endfor%%"){
            Type.FOR_END
        }else if(str.startsWith("%%include'") && str.endsWith("'%%")){
            Type.INCLUDE
        }else if(str.startsWith("%%") && str.endsWith("%%")){
            Type.VARIABLE
        }else{
            Type.Html
        }
    }

    fun map() : List<Container>{
        val idx = text.getIndex("%%")
        var last = -1
        val result = mutableListOf<Container>()
        for(i in idx.indices step 2){
            var start = idx[i]
            var end = idx[i+1]+1
            val html = text.substring(last+1 until start)
            result.add(Container(Type.Html,html))
            last = end
            val txt = text.substring(start..end)
            result.add(Container(getType(txt.replace(" ","")),txt))

        }
        result.add(Container(Type.Html,text.substring(last + 1 until text.length)))
        return result.filter { it.body.isNotBlank() }
    }

}