import server.http.authentication.Authentication
import server.http.authentication.SessionAuthentication
import java.io.File

object Settings {

    var debug : Boolean = true
    var page_404 : String? = null
    var page_403 : String? = null
    var std_path = "templates/"
    var std_auth : List<Authentication> = listOf(SessionAuthentication(false))



}