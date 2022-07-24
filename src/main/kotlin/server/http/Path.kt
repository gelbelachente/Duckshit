package server.http

import Settings
import server.http.authentication.Authentication
import server.http.reponse.HtmlResponse
import server.http.reponse.RedirectResponse
import server.http.reponse.Response
import server.http.reponse.TextResponse
import java.io.File

class Path(val path : String, val type : List<String>,val auth : List<Authentication> = listOf(), val loginUrl : String? = null,
           private val respond : (Request) -> (Response?)){

     fun handle(r : Request) : Response?{
         return if(auth.any { it.authenticate(r) } || auth.isEmpty()){
             auth.forEach {
                 if(r.user == null) {
                     r.user = it.getUser(r)
                 }
             }
             respond(r)
         }else{
             if(loginUrl == null) {
                 if (Settings.page_403 != null) {
                     HtmlResponse(File(Settings.page_403!!))
                 } else {
                     TextResponse("403 Access Denied")
                 }
             }else{
                 RedirectResponse(loginUrl)
             }
         }
     }

 }