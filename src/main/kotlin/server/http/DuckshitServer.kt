package server.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import match
import Settings
import server.db.DuckshitDatabase
import server.db.Search
import server.db.models.Model
import server.db.models.User
import server.forms.ModelForm
import server.http.authentication.Authentication
import server.http.authentication.SessionAuthentication
import server.http.authentication.authenticate
import server.http.reponse.HtmlResponse
import server.http.reponse.RedirectResponse
import server.http.reponse.Response
import transformCookie
import java.io.File
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.*

class DuckshitServer(val port : Int = 6969, val adress : String = "localhost") {

       private val server : HttpServer = HttpServer.create( InetSocketAddress(adress,port), 0)
    private val paths = mutableListOf<Path>()


    fun register(path : String, types : List<String> = listOf("POST","GET","PUT","DELETE","PATCH"), auth : List<Authentication> = listOf(),
                 loginUrl : String? = null,handle : (Request) -> (Response?)){
        paths.add(Path(path,types,auth,loginUrl,handle))
    }

    init {
        DuckshitDatabase.instance
        register("/admin/", listOf("GET"),listOf(SessionAuthentication(isAdmin = true)),"/admin/auth/") {
            val file = File("src/main/kotlin/server/templates/Admin.html")
            HtmlResponse(
                file,
                mutableMapOf("models" to DuckshitDatabase.instance.registered.map { a -> a.toString().split(" ")[1] })
            )
        }
        register("/admin/{model}/", listOf("GET"),listOf(SessionAuthentication(isAdmin = true)),"/admin/auth/") {
            val file = File("src/main/kotlin/server/templates/Admin.html")
            val ctx = mutableMapOf(
                "models" to DuckshitDatabase.instance.registered.map { a -> a.toString().split(" ")[1] },
                "item-title" to it.params["model"]!!,
                "items" to DuckshitDatabase.instance.all(Class.forName(it.params["model"]!!).kotlin as KClass<out Model>)
            )

            HtmlResponse(file, ctx)
        }
        register("admin/{model}/{id}/", listOf("GET"),listOf(SessionAuthentication(isAdmin = true)),"/admin/auth/") {
            val file = File("src/main/kotlin/server/templates/Admin.html")
            val ctx = mutableMapOf(
                "models" to DuckshitDatabase.instance.registered.map { a -> a.toString().split(" ")[1] },
                "item-title" to it.params["model"]!!,
                "model-id" to it.params["id"]!!,
                "form" to ModelForm(
                    Class.forName(it.params["model"]!!).kotlin as KClass<out Model>,
                    listOf("*"),
                    it.params["id"]!!.toInt()
                ).render()
            )
            HtmlResponse(file, ctx)
        }
        register("admin/{model}/{id}/", listOf("POST"),listOf(SessionAuthentication(isAdmin = true)),"/admin/auth/") {
            val params = it.params
            val modelClass = it.params["model"]
            val model = DuckshitDatabase.instance.get(Class.forName(modelClass).kotlin as KClass<out Model>, Search("id = ${params["id"]}"))
            for(p in params.filterKeys { it !in listOf("model","id") }){
                val prop = (model::class.memberProperties.find { it.name == p.key } as KMutableProperty<*>)
                if(prop.returnType.isSubtypeOf(Model::class.createType())){
                    prop.setter.call(model,DuckshitDatabase.instance.get(prop.returnType.classifier as KClass<out Model>,Search("id = ${p.value}")))
                }else {
                    prop.setter.call(model, p.value)
                }
                }
            DuckshitDatabase.instance.update(model)
            RedirectResponse("/admin/${params["model"]}/${params["id"]}/")
        }
        register("admin/{model}/create/", listOf("GET"),listOf(SessionAuthentication(isAdmin = true)),"/admin/auth/"){
            val file = File("src/main/kotlin/server/templates/Admin.html")
            val ctx = mutableMapOf(
                "models" to DuckshitDatabase.instance.registered.map { a -> a.toString().split(" ")[1] },
                "item-title" to it.params["model"]!!,
                "form" to ModelForm(
                    Class.forName(it.params["model"]!!).kotlin as KClass<out Model>,
                    listOf("*"),
                    empty=true
                ).render()
            )
            HtmlResponse(file, ctx)
        }
        register("admin/{model}/create/", listOf("POST"),listOf(SessionAuthentication(isAdmin = true)),"/admin/auth/"){
            val params = it.params
            val modelClass = Class.forName(it.params["model"]).kotlin
            it.params.remove("model")
            val model = modelClass.primaryConstructor!!.call(*(it.params.values.toTypedArray()),0) as Model
            DuckshitDatabase.instance.add(model)
            RedirectResponse("/admin/${modelClass.qualifiedName}/")
        }
        register("admin/auth/", listOf("GET")){
            val file = File("src/main/kotlin/server/templates/Auth.html")
            val ctx = mutableMapOf<String,Any>("user-form" to ModelForm(User::class, listOf("username","password"), empty = true).render)
            HtmlResponse(file,ctx)
        }
        register("admin/auth/", listOf("POST")){
            val username = it.params["username"]!!
            val password = it.params["password"]!!
            if(!authenticate(username,password)) return@register RedirectResponse("/admin/auth/");
            val rep = RedirectResponse("/admin/")
            SessionAuthentication(true).apply(rep,username,password)
            rep
        }
        register("admin/{model}/delete/{id}/", listOf("GET"), listOf(SessionAuthentication(true)),"/admin/auth/"){
            val model = it.params["model"]!!
            val id = it.params["id"]!!
            DuckshitDatabase.instance.delete(Class.forName(model).kotlin as KClass<out Model>,Search("id = $id"))
            RedirectResponse("/admin/$model/")
        }
    }

    private fun resolveQueryParameters(query : String) : MutableMap<String,String>{
        val result  = mutableMapOf<String, String>()
            for (param in query.split("&").filter { it.isNotBlank() }) {
                val entry = param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (entry.size > 1) {
                    result[entry[0]] = entry[1]
                } else {
                    result[entry[0]] = ""
                }
            }
        return result
    }

    private fun getCookies(it : HttpExchange) : List<String> {
        return it.requestHeaders["Cookie"] ?: listOf()
    }

    fun init(){
        server.createContext("/"){
            val os: OutputStream = it.responseBody

           val site = paths.filter { s -> s.type.contains(it.requestMethod) }.filter { s -> it.requestURI.match(s.path) != Integer.MAX_VALUE }
               .minByOrNull { s -> it.requestURI.match(s.path) }
               ?: run {
                   //Debug Panel / 404
                   if(Settings.debug) {
                       val file = File("src/main/kotlin/server/templates/Debug.html")
                       val rep = HtmlResponse(
                           file,
                           mutableMapOf("path" to it.requestURI.path, "paths" to paths, "type" to it.requestMethod)
                       )
                       val body = rep.resolve()
                       it.sendResponseHeaders(404, body.length.toLong())
                       os.write(body.toByteArray())
                   }else if(Settings.page_404 != null){
                           val file = File(Settings.page_404!!)
                           val response = HtmlResponse(file)
                           val body = response.resolve()
                           it.responseHeaders["SET-COOKIE"] = response.getNewCookies()
                           it.sendResponseHeaders(404, body.length.toLong())

                           os.write(body.toByteArray())
                       }
                   os.close()
                   return@createContext
               }
                try{
                val params = resolveQueryParameters((it.requestURI.query ?: "") +
                        if(it.requestMethod == "POST") "&" + String(it.requestBody.readAllBytes(),StandardCharsets.UTF_8) else "")
            val cookies = getCookies(it)
                // processing
            val response = site.handle(Request(it.requestMethod,site.path,it.requestURI.path, params,
                getCookies(it).map { transformCookie(it) },it.requestHeaders, site.auth))
            if(response != null) {
                val body = response.resolve()
                if(response is RedirectResponse){
                    it.responseHeaders.add("Location",response.redirect)
                }
                response.getNewCookies().forEach {c ->
                    it.responseHeaders.add("SET-COOKIE",c)
                }
                it.sendResponseHeaders(response.status, body.length.toLong())
                os.write(body.toByteArray())

            }
                }catch (e : Exception){
                    println(e.stackTraceToString() + e.message + e.cause)
                }
            os.close()
        }
        server.setExecutor(null); // creates a default executor
        server.start();
        println("Duckshit Server started: http://$adress:$port/")
    }

}


