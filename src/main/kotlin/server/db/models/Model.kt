package server.db.models


open class Model(@Sql("id",true,true) var id : Int = 0){
    override fun toString(): String {
        return this.id.toString()
    }
}


