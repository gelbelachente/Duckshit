
import server.db.DuckshitDatabase
import server.db.models.Model
import server.db.models.Sql
import server.db.models.User
import server.http.DuckshitServer
import server.terminal.TerminalListener



fun main() {

val user = User("Mario","Pepe123",true)
    DuckshitDatabase.instance.apply {
        register<User>(false)
        //add(G(user))
        //add(G(User("alpha","beta")))
    }
    //DuckshitServer().init()
    DuckshitServer().init()

    //TerminalListener().listen()

}
