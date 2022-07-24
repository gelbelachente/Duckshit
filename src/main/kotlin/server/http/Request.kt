package server.http

import com.sun.net.httpserver.Headers
import server.db.models.User
import server.http.authentication.Authentication

class Request(
    val type: String, val path: String, val uri: String, val params: MutableMap<String, String> = mutableMapOf(),
    val cookies: List<Cookie> = listOf(),
    val requestHeaders: Headers, val auth : List<Authentication>, var user : User? = null
){



    init{
        val u = uri.split("/").filterNot { it.isBlank() }
        val p = path.split("/").filterNot { it.isBlank() }
        for(i in u.indices){
                if(p[i].first() == '{' && p[i].last() == '}') {
                  params[p[i].substring(1 until p[i].length-1)] = u[i]
                }
        }
    }


}