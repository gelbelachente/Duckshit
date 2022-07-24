package server.forms

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import server.forms.fields.form

internal class FormTest {

    lateinit var f : Form

    @BeforeEach
    fun setup(){
        f = Form(listOf(
            form.IntField("ab123",null),
            form.CharField("2f3ff9ß!)$/§)/§$","asdfasdf"),
            form.IntField("sdfdsf","-234234")
        ))
    }


    @Test
    fun render() {
        org.junit.jupiter.api.assertDoesNotThrow {
            f.render()
            f.renderWithoutValue()
        }
        org.junit.jupiter.api.assertThrows<Exception> {
            f.render("FUCK YOU")
        }
    }




}