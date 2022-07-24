package server.template

enum class Type(){
    Html,
    VARIABLE,
    CONDITION_START,
    CONDITION_ELSE,
    CONDITION_END,
    FOR_START,
    FOR_END,
    INCLUDE;


    fun isCondition() = this in listOf(CONDITION_END, CONDITION_ELSE, CONDITION_START)

}