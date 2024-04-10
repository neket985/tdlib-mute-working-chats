package ru.smirnov.muteworkingchats.util

import ru.smirnov.muteworkingchats.Client

val defaultHandler = Client.ResultHandler { obj ->
    print(obj.toString())
}
