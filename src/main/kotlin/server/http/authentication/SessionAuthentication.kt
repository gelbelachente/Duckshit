package server.http.authentication

import server.db.DuckshitDatabase
import server.db.Search
import server.db.models.User
import server.http.Request
import server.http.SameSite
import server.http.reponse.Response
import kotlin.random.Random
import kotlin.random.nextInt

class SessionAuthentication(isAdmin : Boolean) : Authentication(isAdmin){

   companion object{
       init{
           DuckshitDatabase.instance.register(Session::class)
       }
   }
    private fun session(user : User) : String = user.id.toString() + Random.nextInt(1000 ..999_999).toString()

    override fun apply(r: Response, username: String, password: String) {
        val user = DuckshitDatabase.instance.get(
            User::class,
            Search("username = '$username'") * Search("password = '$password'")
        ) as User
        val id : String
        if(DuckshitDatabase.instance.getOrNull(Session::class, Search("user = ${user.id}")) == null){
            id = session(user)
            DuckshitDatabase.instance.add( Session(user, id))
        }else{
            id = (DuckshitDatabase.instance.get(Session::class, Search("user = ${user.id}")) as Session).sessionId
        }
        r.addCookie("SESSION",id, path = "/", sameSite = SameSite.none)
        }

    override fun authenticate(r : Request): Boolean {
        val c = r.cookies.find { it.name == "SESSION" } ?: return false
        val session =  DuckshitDatabase.instance.getOrNull(Session::class, Search("sessionId = ${c.value}")) as Session? ?: return false
        return (isAdmin && session.user.isAdmin) || !isAdmin

    }

    override fun getUser(r: Request): User? {
        val id = r.cookies.find { it.name == "SESSION" } ?: return null
        val session =  DuckshitDatabase.instance.getOrNull(Session::class, Search("sessionId = ${id.value}")) as Session? ?: return null
        return if(isAdmin && !session.user.isAdmin) null else session.user
    }

}