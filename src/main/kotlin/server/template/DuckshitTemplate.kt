package server.template

import getVariable
import Settings
import java.io.File
import java.io.FileNotFoundException

class DuckshitTemplate(val file : File, val ctx : MutableMap<String,Any>){

        private fun validateFile(){
            if(!file.exists()){
                throw FileNotFoundException("Duckshit couldn't find the file at ${file.path}!")
            }
            if(!file.absolutePath.endsWith(".html")){
                error("Duckshit only renders .html files!")
            }
        }


        private fun compare(val1 : Any, op : String, val2 : Any) : Boolean{
            return when(op){
                "==" -> {
                    val1.equals(val2)
                }
                "!=" -> {
                    !val1.equals(val2)
                }
                else ->{
                    error("$op no compare-operator")
                }
            }
        }

    fun substitute(str : String) : String{
        val name = str.replace(" ","").replace("%%","".trim()).split(".").filter { it.isNotBlank() }
        val value = getVariable(name.first(),ctx,name.drop(1))
        return value.toString()
    }
    fun include(str : String) : String{
        val name = str.replace("%%","").replace("'","").trim()
        val file = File(Settings.std_path + "name")
        if(!file.exists()){
            println( "\\033[45m" + "$str does not exist as a file in ${Settings.std_path}")
        }
        return file.readText()
    }

    fun resolveCondition(str : String) : Boolean{
        val base = str.replace(" ","").replace("%%if","").replace("'","").trim()
        val idx = base.lastIndexOf('=')
        val op = base.substring(idx-1 .. idx)
        val first = base.substring(0 until idx-1).split(".").filter { it.isNotBlank() }
        val last = base.substring(idx+1 until base.length).split(".").filter { it.isNotBlank() }
        return compare(getVariable(first.first(),ctx,first.drop(1)), op, getVariable(last.first(), ctx, last.drop(1)))
    }

    data class MapAttribute(val key : Any?, val value : Any?)

    fun resolveIterable(value : Any) : List<Any>{
        return if(value is Iterable<*>){
            (value as Iterable<Any>).toList()
        }else if(value is Map<*, *>){
            (value as Map<*, *>).map { MapAttribute(it.key,it.value) }
        }else{
            listOf()
        }
    }

    fun resolveLoop(str : String) : Looper {
        val base = str.replace("%%","").trim().drop(3)
        val (name,_,iterator) = base.split(" ").filter { it.isNotBlank() }
        if(iterator.trim().startsWith("range(") && iterator.trim().endsWith(")")){
            val number = iterator.replace("range(","").replace(")","").trim().toInt()
            val iter = buildList { for(i in 0 until number){ add(i) } }
            return Looper(iter,name)
        }
        val iter = iterator.split(".").filter { it.isNotBlank() }
        val value = getVariable(iter.first(),ctx,iter.drop(1))
        return Looper(resolveIterable(value),name)
    }

    data class Looper(val items : List<Any>, val name : String, val content : MutableList<Container> = mutableListOf(),
                      var blocked : Boolean = false)
    val loops = mutableListOf<Looper>()
    val conditions = mutableListOf<Boolean>()

    fun render(lexed : List<Container> = Lexer(file.readText()).map()) : String{
        var txt = ""
        for(c in lexed){
            if (conditions.isNotEmpty() && conditions.any { !it } && !c.type.isCondition() ){
                continue
            }
            if(loops.any { !it.blocked }){
                loops.filter { !it.blocked }.forEach { it.content.add(c) }
                if(c.type != Type.FOR_START && c.type != Type.FOR_END){
                    continue
                }
                if(c.type == Type.FOR_END){
                    loops[loops.size-1].content.removeLast()
                }
            }
            when(c.type) {
                Type.Html -> {
                    txt += c.body
                }
                Type.VARIABLE -> {
                    txt += substitute(c.body)
                }
                Type.INCLUDE -> {
                    txt += include(c.body)
                }
                Type.CONDITION_START -> {
                    conditions.add(resolveCondition(c.body))
                }
                Type.CONDITION_ELSE -> {
                    conditions[conditions.size - 1] = !conditions.last()
                }
                Type.CONDITION_END -> {
                    conditions.removeLast()
                }
                Type.FOR_START -> {
                    loops.add(resolveLoop(c.body))
                }
                Type.FOR_END -> {
                    if (loops.filter { !it.blocked }.size > 1) {
                        loops.removeLast()
                    }else if (loops.filter { !it.blocked }.size == 1) {
                        val loop = loops.last()
                        loop.blocked = true
                        for ((idx, item) in loop.items.withIndex()) {
                            ctx[loop.name] = item
                            txt += render(loop.content.toList())
                        }
                        ctx.remove(loop.name)
                        loops.removeLast()
                    }
                }
                else -> {}
            }
        }
        return txt
    }


    fun transform(content : String = "", ){

    }





}