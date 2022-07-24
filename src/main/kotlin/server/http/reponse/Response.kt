package server.http.reponse

import Settings
import server.http.Cookie
import server.http.Request
import server.http.SameSite
import java.sql.Date

abstract class Response(var status : Int = 200, private val cookies : MutableMap<String, Cookie> = mutableMapOf()){
    abstract fun resolve() : String
    fun addCookie(key : String, value : String, expires : Date? = null, maxAge : String? = null, domain : String? = null,
                  path : String? = null, sameSite : SameSite = SameSite.STRICT){
        cookies[key] = Cookie(key,value, expires?.toGMTString(),maxAge,domain,path,sameSite)
    }

    fun getNewCookies() : List<String> = cookies.map { it.value.toString() }

    fun login(username : String, password : String,request: Request){
        Settings.std_auth.forEach {
            it.apply(this,username,password)
        }
    }

}