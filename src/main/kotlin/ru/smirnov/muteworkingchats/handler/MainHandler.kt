package ru.smirnov.muteworkingchats.handler

import ru.smirnov.muteworkingchats.AuthorizationStateHolder
import ru.smirnov.muteworkingchats.Client
import ru.smirnov.muteworkingchats.TdApi

class MainHandler : Client.ResultHandler {
    override fun onResult(obj: TdApi.Object) {
        when (obj.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                AuthorizationStateHolder.onAuthorizationStateUpdated((obj as TdApi.UpdateAuthorizationState).authorizationState)
            }
            else -> {}
        }
    }
}
