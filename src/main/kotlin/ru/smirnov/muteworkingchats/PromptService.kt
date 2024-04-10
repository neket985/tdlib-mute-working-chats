package ru.smirnov.muteworkingchats

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object PromptService {
    fun promptString(prompt: String): String {
        print(prompt)
        val reader = BufferedReader(InputStreamReader(System.`in`))
        var str = ""
        try {
            str = reader.readLine()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return str
    }

    private fun print(str: String?) {
        println("")
        println(str)
    }
}
