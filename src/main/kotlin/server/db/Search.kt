package server.db

class Search(var str : String){

    operator fun times( b : Search) : Search {
        this.str += " AND " + b.str
        return this
    }

    operator fun div( b : Search) : Search {
        this.str += " OR " + b.str
        return this
    }

    fun query() = "WHERE $str"

}