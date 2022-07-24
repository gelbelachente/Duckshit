package server.db

import org.junit.jupiter.api.*
import server.db.models.Model
import server.db.models.Sql
import server.db.models.User

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DuckshitDatabaseTest {

    lateinit var db : DuckshitDatabase
    lateinit var user : Ex2

    @BeforeAll
    fun setUp() {
        db = DuckshitDatabase.instance
        user = Ex2("alpha")
    }

     class Ex(@Sql("name_xyz") val name : String, @Sql("user") val user : Ex2, id : Int = 0) : Model(id)
    class Ex2(@Sql("name_xyz") var name : String, id : Int = 0) : Model(id)

    @Order(0)
    @Test
    fun register() {
        db.register<Ex>(true)
        db.register<Ex2>(true)
        assert(db.registered.contains(Ex::class) && db.registered.contains(Ex2::class))
    }
    @Order(1)
    @Test
    fun add() {
        assertThrows<Exception> {
            db.add(Ex("\"F\'", user))
        }
        val ex = Ex("abc",user)
        db.add(ex)
        //missing id change after add
        assert(ex.id > 0)
    }

    @Order(2)
    @Test
    fun all() {
        assert( db.all<Ex>().size == 1)
    }

    @Order(3)
    @Test
    fun filter() {
        assert(db.filter<Ex>(Search("name_xyz = 'abc'") / Search("name_xyz = 'treize'")).size == 1)

    }

    @Order(4)
    @Test
    fun size() {
        assert(db.size<Ex>() == 1)
    }


    @Order(5)
    @Test
    fun get() {
        assertThrows<Exception> {
            db.get<Ex>(Search("name_xyz = 'äöü'"))
        }
        assertDoesNotThrow {
            (db.get<Ex>(Search("name_xyz = 'abc'")))
        }
    }


    @Order(6)
    @Test
    fun getOrNull() {
        assert(db.getOrNull<Ex>(Search("name_xyz = 'sdfsdf'")) == null)
    }

    @Order(7)
    @Test
    fun update() {
        val a = db.get<Ex>(Search("name_xyz = 'abc'")) as Ex
        a.user.name = "beta"
        assertDoesNotThrow {
            db.update(a)
        }
    }

    @Order(8)
    @Test
    fun delete() {
        db.delete<Ex>(Search("name_xyz = 'abc'"))
        assert(db.size<Ex>() == 0)
    }

    @AfterAll
    fun setDown(){
        //db is locked error
        return
        db.unregister<Ex>()
        db.unregister<Ex2>()
    }

}