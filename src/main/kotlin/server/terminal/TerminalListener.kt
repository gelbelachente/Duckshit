package server.terminal

import server.db.DuckshitDatabase
import server.db.models.User
import kotlin.system.exitProcess

class TerminalListener() {

    fun listen(){
        val cmd = readln()
        execute(cmd)
        listen()
    }

    private fun find(str : String, find : Char) : List<Int>{
        val result = mutableListOf<Int>()
        str.forEachIndexed {i,it->
                if(it == find){
                    result.add(i)
                }
        }
        return result
    }

    private fun execute(cmd : String){
        var x = cmd.replace(" ","")

        if(x == "exit"){
            exitProcess(0)

        } else if(x.startsWith("create-admin")){
            x = x.removePrefix("create-admin")
            val res = find(x,'\"')
            if(res.size != 4){
                println("The username and the password must be separated by a \"")
                return
            }

            val username = x.substring(res[0]+1,res[1])
            val password = x.substring(res[2]+1,res[3])

            if(username.isEmpty() || password.isEmpty()){
                println("Credentials mustn't be blank!")
                return
            }
            DuckshitDatabase.instance.apply {
                register<User>()
                add(User(username,password,true))
            }
            println("Admin created!")
        }

    }


}