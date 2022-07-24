package server.db.models



annotation class Sql(val name : String, val pk : Boolean = false, val auto : Boolean = false,
                     val onDelete : OnDelete = OnDelete.REMOVE){

}