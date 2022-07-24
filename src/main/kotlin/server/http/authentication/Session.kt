package server.http.authentication

import server.db.models.Model
import server.db.models.Sql
import server.db.models.User

class Session(@Sql("user") val user : User, @Sql("sessionId") val sessionId : String, id : Int = 0)
    : Model(id)