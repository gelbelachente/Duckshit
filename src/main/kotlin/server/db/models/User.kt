package server.db.models

 class User(@Sql("username") var username : String, @Sql("password") var password : String, @Sql("is_admin") val isAdmin : Boolean = false,
            id : Int = 0) : Model(id){

     override fun toString(): String {
         return username
     }

            }