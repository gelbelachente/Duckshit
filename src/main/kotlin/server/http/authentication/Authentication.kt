package server.http.authentication

import server.db.DuckshitDatabase
import server.db.Search
import server.db.models.User
import server.http.Request
import server.http.reponse.Response

fun authenticate(username : String, password : String) =
    DuckshitDatabase.instance.getOrNull(User::class, Search("username = '$username'") * Search("password = '$password'")) != null

fun register(username: String, password: String){
    val u = User(username, password)
    DuckshitDatabase.instance.add(u)
}

abstract class Authentication(val isAdmin : Boolean) {

    abstract fun apply(r : Response, username: String, password: String);
    abstract fun authenticate(r : Request) : Boolean
    abstract fun getUser(r: Request) : User?

}