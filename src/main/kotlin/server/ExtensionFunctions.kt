
import server.db.models.Model
import server.db.models.User
import server.http.Cookie
import server.http.SameSite
import java.net.URI
import kotlin.reflect.KProperty1

fun String.getIndex(str : String) : List<Int>{
    val result = mutableListOf<Int>()
    for(i in 0 .. (this.length-str.length)){
        if(this.substring(i until(i+str.length)) ==  str){
                result.add(i)
        }
    }
    return result
}

fun Any.get(name : String) : Any{
    return (this::class.members.first { it.name == name } as KProperty1<Any, *>).get(this)!!
}

 fun getVariable(name : String,ctx : MutableMap<String,Any>, extras : List<String> = listOf()) : Any{
     var value = ctx[name] ?: return "null"

     for(extra in extras){
        value = value.get(extra)
    }
    return value
}

fun Any.name() = this::class.java.name.split(".").last()

 fun Any.convert() : String{
    if(this is String){
        return "'$this'"
    }else if(this is Model){
        return this.id.toString()
    }
    return this.toString()
}

fun transformCookie(str: String) : Cookie{
    val(key,value) = str.split("=").filter { it.isNotBlank() }.map { it.trim() }
    return Cookie(key,value,"","","","")
}

fun URI.match(path : String) : Int{
    val u = this.path.split("/").filterNot { it.isBlank() }.map { it.trim() }
    val p = path.split("/").filterNot { it.isBlank() }.map { it.trim() }
    if(u.size != p.size)return Integer.MAX_VALUE;
    val b = mutableListOf<Int>()
    for(i in u.indices){
        if(p[i] == u[i]){
            b.add(2)
        }else
            if(p[i].first() == '{' && p[i].last() == '}'){
                b.add(1)
            }else{
                b.add(0)
            }
    }
    if(b.all { it == 2 }) return -1;
    if(b.any { it == 0 }) return Integer.MAX_VALUE;
    return b.filter { it == 1 }.size
}

