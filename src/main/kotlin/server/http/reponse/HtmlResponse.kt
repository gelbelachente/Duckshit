package server.http.reponse

import server.template.DuckshitTemplate
import java.io.File

class HtmlResponse(val file : File, val ctx : MutableMap<String,Any> = mutableMapOf()) : Response(){
    override fun resolve(): String {
        return DuckshitTemplate(file, ctx).render()
    }

}