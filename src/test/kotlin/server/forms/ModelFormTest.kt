package server.forms

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import server.db.models.Model
import server.db.models.Sql
import server.db.models.User

internal class ModelDeserializerTest {


    class Tester(@Sql("a") val a : Int, id : Int = 0) : Model(id)

    @Test
    fun deserialize() {
    val mf = ModelDeserializer(User::class)
        assertDoesNotThrow {
            mf.deserialize(mapOf("username" to "123","password" to "657","id" to 234234))
            mf.deserialize(mapOf("id" to 32452345,"password" to "988fsdf","username" to "sdfsfd"))
        }

    }
}