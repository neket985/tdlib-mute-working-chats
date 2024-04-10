package ru.smirnov.muteworkingchats

import kotlin.system.exitProcess

object PropertiesHolder {
    private val file = this::class.java.classLoader.getResourceAsStream("application.properties")

    private val propsMap = if (file != null) {
        file.readAllBytes().let { String(it) }
            .split("\n")
            .map { it.split("=", limit = 2) }
            .map { (k, v) -> k.trim() to v.trim() }
            .toMap()
    } else {
        println("Properties file does not exists")
        exitProcess(-1)
    }

    val tgPhone = propsMap.getOrThrow("account.telegram.phone")

    private fun Map<String, String>.getOrThrow(key: String) = this[key]
        ?: error("Не найдено значение для свойства [$key]")
}
