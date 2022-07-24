package server.http.reponse

class TextResponse(val text : String) : Response() {
    override fun resolve(): String {
        return text
    }
}