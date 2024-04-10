package ru.smirnov.muteworkingchats

import kotlin.system.exitProcess

object PropertiesHolder {
    private val file = this::class.java.classLoader.getResourceAsStream("application.properties")

    private val templatePattern = Regex("""\$\{(?<prop>[^}]+)\}""")
    private val propsMap = if (file != null) {
        val propsFromEnv = System.getenv()
        val propsFromSystem = System.getProperties()
            .mapNotNull {
                val k = it.key.toString()
                val v = it.value?.toString() ?: return@mapNotNull null
                k to v
            }
            .toMap()

        val propsFromFile = file.readAllBytes().let { String(it) }
            .split("\n")
            .map { it.split("=", limit = 2) }
            .map { (k, v) ->
                val solvedValue = v.trim().replace(templatePattern) {
                    val propName = it.groups["prop"]!!.value
                    propsFromEnv[propName] ?: propsFromSystem[propName]!!
                }
                k.trim() to solvedValue
            }
            .toMap()

        propsFromEnv.plus(propsFromSystem).plus(propsFromFile)
    } else {
        println("Properties file does not exists")
        exitProcess(-1)
    }

    val tgPhone = propsMap.getOrThrow("account.telegram.phone")
    val tgPassword = propsMap.getOrThrow("account.telegram.password")

    private fun Map<String, String>.getOrThrow(key: String) = this[key]
        ?: error("Не найдено значение для свойства [$key]")
}
