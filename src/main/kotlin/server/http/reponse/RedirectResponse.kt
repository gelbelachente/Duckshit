package server.http.reponse

class RedirectResponse(val redirect : String) : Response(303) {
    override fun resolve(): String {
        return "redirected to $redirect"
    }


}