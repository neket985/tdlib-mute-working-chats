package ru.smirnov.muteworkingchats.util

import ru.smirnov.muteworkingchats.Client
import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.holder.AuthorizationStateHolder

val defaultHandler = Client.ResultHandler { obj ->
    print(obj.toString())
}

val requestHandler = Client.ResultHandler { obj ->
    when (obj.constructor) {
        TdApi.Error.CONSTRUCTOR -> {
            System.err.println("Receive an error:" + newLine + obj)
            AuthorizationStateHolder.onAuthorizationStateUpdated(null) // repeat last action
        }
        TdApi.Ok.CONSTRUCTOR -> {}
        else -> System.err.println("Receive wrong response from TDLib:" + newLine + obj)
    }
}
