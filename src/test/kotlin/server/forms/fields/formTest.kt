package server.forms.fields

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class formTest{

     @Test
     fun intFieldTest(){
         val x = form.IntField("abcd","12345")
         x.apply("id","ROCCO CHAZE")
         x.apply("FOLGORE")

         val y = form.IntField("sdf","ALFA DEVO FARE")
         val z = form.IntField("sdf",null)
         org.junit.jupiter.api.assertDoesNotThrow {
             x.renderWithoutValue()
             x.render()

             z.render()
             z.renderWithoutValue()
         }
         org.junit.jupiter.api.assertThrows<Exception> {
             y.render()
             y.renderWithoutValue()
         }
     }

    @Test
    fun intCharTest(){
        val x = form.CharField("abcd","sdfasdf")
        x.apply("id","ROCCO CHAZE")
        x.apply("FOLGORE")

        val y = form.CharField("sdf",null)
        org.junit.jupiter.api.assertDoesNotThrow {
            x.renderWithoutValue()
            x.render()

            y.render()
            y.renderWithoutValue()
        }
    }


}