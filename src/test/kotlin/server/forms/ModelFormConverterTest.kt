package server.forms

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import server.db.models.Model
import server.db.models.Sql
import server.db.models.User

internal class ModelFormConverterTest {


    class Tester(@Sql("name_xyz")val name : String, @Sql("user") val user : User, id : Int = 0) : Model(id)

    @Test
    fun resolveFields() {
        val u1 = User("Rawiwa","FIFA")
        val t1 = Tester("alfa",u1)
        val c1 = ModelFormConverter(Tester::class, listOf("*"),t1)
        assertDoesNotThrow{
            val rs = c1.resolveFields()
        }
    }
}