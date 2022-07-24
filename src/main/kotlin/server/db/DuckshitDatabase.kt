package server.db

import convert
import get
import server.db.models.DatabaseConverter
import server.db.models.Model
import server.db.models.OnDelete
import server.db.models.Sql
import java.sql.Connection
import java.sql.DriverManager
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*


class DuckshitDatabase() {


    companion object{
        var instance = DuckshitDatabase()
    }


   private lateinit var connection : Connection
     val registered  = mutableListOf<KClass<out Model>>()

    inline fun <reified T : Model>register(update : Boolean = false){
        register(T::class,update)
    }

    inline fun <reified T : Model>unregister(){
      unregister(T::class)
    }

    private fun getConnection() : Connection{
        val url = "jdbc:sqlite:duckshit.main.sqlite"
       return DriverManager.getConnection(url)
    }

    fun unregister(model : KClass<out Model>){
        val sql = "DROP TABLE IF EXISTS " + model.simpleName
        connection.createStatement().execute(sql)
        registered.remove(model)
    }

    fun register(model : KClass<out Model>, update : Boolean = false){
        if(update){
            unregister(model)
        }
        val sql = DatabaseConverter(model).createData(update)
        connection.createStatement().execute(sql)
        registered.add(model)
    }

    private fun getParameter(model : KClass<out Model>) : List<KParameter>{
        return (model.primaryConstructor!!.parameters)
            }


    private fun getAnnotationParameter(model : KClass<out Model>) : List<Sql>{
        return getParameter(model).filter { it.findAnnotations(Sql::class).size == 1 }.map { it.findAnnotations(Sql::class).first() }
    }

    fun max(m : KClass<out Model>, column : String = "id") : Int{
        val sql = "SELECT MAX($column) FROM ${m.simpleName}"
        return connection.createStatement().executeQuery(sql).getInt(1)
    }

    fun add(m : Model){
        val model = m::class
        val fields = getParameter(model).filter {it.findAnnotations(Sql::class).size == 1 &&
                !it.findAnnotations(Sql::class).first().auto }
        fields.filter { it.type.isSubtypeOf(Model::class.createType()) }.forEach {
            val i = m.get(it.name!!) as Model
            if(i.id == 0 ||getOrNull(it.type.classifier as KClass<out Model>,Search("id = ${i.id}")) == null){
                add(i)
                i.id = max(i::class)
            }
        }
        //INSERT INTO USER (username,password) Values('a','b')
        val sql = "INSERT INTO ${model.java.simpleName} ("+
                fields.joinToString(","){it.findAnnotations(Sql::class).first().name} +
                 ")\nValues("+
                   fields.joinToString(","){m.get(it.name!!).convert()} + ")"
        connection.createStatement().execute(sql)
        m.id = max(model)
    }

    inline fun <reified T : Model>all() : List<Model>{
        return all(T::class)
    }

    fun all(model : KClass<out Model>) : List<Model>{
        val sql = "SELECT * FROM ${model.java.simpleName}"
        val rs = connection.createStatement().executeQuery(sql)
        val result = mutableListOf<Model>()
        val fields = getParameter(model)
        first@ while (rs.next()){
            val values = mutableMapOf<KParameter,Any?>()
            for(s in fields){
                val x : String = s.findAnnotations(Sql::class).firstOrNull()?.name ?: s.name!!
                values[s] = rs.getObject(x)
            }
            second@ for((i,p) in model.primaryConstructor!!.parameters.withIndex()){
                if(p.type.isSubtypeOf(Model::class.createType())){
                    val z = getOrNull(p.type.classifier as KClass<out Model>,Search("id = ${values[p]}")) as Model?
                    if(z == null && p.findAnnotations(Sql::class).first().onDelete == OnDelete.REMOVE){
                        delete(model,Search("id = ${values.firstNotNullOf { it.key.name == "id" }}"))
                       continue@first
                    }
                    values[p] = z                }
                if(p.type == Boolean::class.createType()){
                    values[p] = (values[p] as Int).toInt() != 0
                }
            }
            result.add(model.primaryConstructor!!.callBy(values))
        }
        return result
    }

    inline fun <reified T : Model>filter(q : Search) : List<Model>{
        return filter(T::class,q)
    }

    fun filter(model : KClass<out Model>,q : Search) : List<Model> {
        val sql = "SELECT * FROM ${model.java.simpleName}\n" + q.query()
        val rs = connection.createStatement().executeQuery(sql)
        val result = mutableListOf<Model>()
        val fields = getParameter(model)

        first@ while (rs.next()) {
            val values = mutableMapOf<KParameter,Any?>()
            for(s in fields){
                val x : String = s.findAnnotations(Sql::class).firstOrNull()?.name ?: s.name!!
                values[s] = rs.getObject(x)
            }
            second@ for((i,p) in model.primaryConstructor!!.parameters.withIndex()){
                if(p.type.isSubtypeOf(Model::class.createType())){
                    val z = getOrNull(p.type.classifier as KClass<out Model>,Search("id = ${values[p]}")) as Model?
                    if(z == null && p.findAnnotations(Sql::class).first().onDelete == OnDelete.REMOVE){
                        delete(model,Search("id = ${values.firstNotNullOf { it.key.name == "id" }}"))
                        continue@first
                    }
                    values[p] = z                }
                if(p.type == Boolean::class.createType()){
                    values[p] = (values[p] as Int).toInt() != 0
                }
            }
                result.add(model.primaryConstructor!!.callBy(values))
            }
            return result
        }

        inline fun <reified T : Model>size() : Int{
            return size(T::class)
        }

        fun size(model: KClass<out Model>): Int {
            val sql = "SELECT COUNT(*) FROM ${model.java.simpleName}"
            val rs = connection.createStatement().executeQuery(sql)
            return rs.getInt(1)
        }

        inline fun <reified T : Model>delete(q : Search){
            delete(T::class,q)
        }

        fun delete(model: KClass<out Model>, q: Search) {
            val sql = "DELETE FROM ${model.java.simpleName}\n" + q.query()
            connection.createStatement().execute(sql)
        }

        inline fun <reified T : Model>get(q : Search) : Model{
            return get(T::class,q)
        }

        fun get(model: KClass<out Model>, q: Search): Model {
            return getOrNull(model, q)!!
        }


        fun update(model: Model) {
            val sql = "Update ${model::class.java.simpleName}\nSET " +
                    getParameter(model::class).filter { it.name != "id" }.joinToString(",")
                    {
                        it.findAnnotations(Sql::class).first().name + "=" + model.get(it.name!!).convert()
                    } + "\nWHERE id = " + model.id
            connection.createStatement().execute(sql)
        }


        inline fun <reified T : Model>getOrNull(q : Search) : Model?{
            return getOrNull(T::class,q)
        }

        fun getOrNull(model: KClass<out Model>, q: Search): Model? {
            val sql = "SELECT * FROM ${model.java.simpleName}\n" + q.query()
            val rs = connection.createStatement().executeQuery(sql)
            val result = mutableListOf<Model>()
            val fields = getParameter(model)
            val pc = model.primaryConstructor!!
            if (rs.next()) {
                val values = mutableMapOf<KParameter,Any?>()
                for (s in fields) {
                    val x : String = s.findAnnotations(Sql::class).firstOrNull()?.name ?: s.name!!
                    values[s] = rs.getObject(x)
                }
                for((i,p) in model.primaryConstructor!!.parameters.withIndex()){
                    if(p.type.isSubtypeOf(Model::class.createType())){
                        val z = getOrNull(p.type.classifier as KClass<out Model>,Search("id = ${values[p]}")) as Model?
                            if(z == null && p.findAnnotations(Sql::class).first().onDelete == OnDelete.REMOVE){
                                delete(model,Search("id = ${values.firstNotNullOf { it.key.name == "id" }}"))
                                return null
                            }
                        values[p] = z
                    }
                    if(p.type == Boolean::class.createType()){
                        values[p] = (values[p] as Int).toInt() != 0
                    }
                }
                return model.primaryConstructor!!.callBy(values)
            }
            return null
        }


        init {
            val url = "jdbc:sqlite:duckshit.main.sqlite"
            connection = DriverManager.getConnection(url)

            println("Connection to ${connection.metaData.driverName} Database was established")

        }

    }


