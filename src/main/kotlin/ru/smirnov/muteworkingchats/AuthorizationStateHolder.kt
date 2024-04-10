package ru.smirnov.muteworkingchats

import ru.smirnov.muteworkingchats.util.defaultHandler
import ru.smirnov.muteworkingchats.util.newLine
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

object AuthorizationStateHolder {
    private var authorizationState: TdApi.AuthorizationState? = null
    private val authorizationLock: Lock = ReentrantLock()
    private val gotAuthorizationLock = authorizationLock.newCondition()

    @Volatile
    private var haveAuthorization = false

    @Volatile
    private var needQuit = false

    @Volatile
    private var canQuit = false

    private val requestHandler = Client.ResultHandler { obj ->
        when (obj.constructor) {
            TdApi.Error.CONSTRUCTOR -> {
                System.err.println("Receive an error:" + newLine + obj)
                onAuthorizationStateUpdated(null) // repeat last action
            }
            TdApi.Ok.CONSTRUCTOR -> {}
            else -> System.err.println("Receive wrong response from TDLib:" + newLine + obj)
        }
    }

    fun waitGotAuthorisation() {
        authorizationLock.lock()
        try {
            while (!haveAuthorization) {
                gotAuthorizationLock.await()
            }
        } finally {
            authorizationLock.unlock()
        }
    }

    fun logout() {
        haveAuthorization = false
        ClientHolder.getClient().send(TdApi.LogOut(), defaultHandler)
    }

    fun quit() {
        needQuit = true
        haveAuthorization = false
        ClientHolder.getClient().send(TdApi.Close(), defaultHandler)
    }

    fun onAuthorizationStateUpdated(authorizationStateParam: TdApi.AuthorizationState?) {
        if (authorizationStateParam != null) {
            this.authorizationState = authorizationStateParam
        }
        val authorizationState = authorizationState!!

        when (authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val request: TdApi.SetTdlibParameters = TdApi.SetTdlibParameters()
                request.databaseDirectory = "tdlib"
                request.useMessageDatabase = true
                request.useSecretChats = true
                request.apiId = 94575
                request.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"
                request.systemLanguageCode = "en"
                request.deviceModel = "Desktop"
                request.applicationVersion = "1.0"
                ClientHolder.getClient().send(request, requestHandler)
            }
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                val phoneNumber = PropertiesHolder.tgPhone
                println("Entered phone number automatically [$phoneNumber]")
                ClientHolder.getClient().send(
                    TdApi.SetAuthenticationPhoneNumber(phoneNumber, null),
                    requestHandler,
                )
            }
            TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                val link: String = (authorizationState as TdApi.AuthorizationStateWaitOtherDeviceConfirmation).link
                println("Please confirm this login link on another device: $link")
            }
            TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR -> {
                val emailAddress = PromptService.promptString("Please enter email address: ")
                ClientHolder.getClient().send(TdApi.SetAuthenticationEmailAddress(emailAddress), requestHandler)
            }
            TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR -> {
                val code = PromptService.promptString("Please enter email authentication code: ")
                ClientHolder.getClient().send(
                    TdApi.CheckAuthenticationEmailCode(TdApi.EmailAddressAuthenticationCode(code)),
                    requestHandler,
                )
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                val code = PromptService.promptString("Please enter authentication code: ")
                ClientHolder.getClient().send(TdApi.CheckAuthenticationCode(code), requestHandler)
            }
            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                val password = PromptService.promptString("Please enter password: ")
                ClientHolder.getClient().send(TdApi.CheckAuthenticationPassword(password), requestHandler)
            }
            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                haveAuthorization = true
                authorizationLock.lock()
                try {
                    gotAuthorizationLock.signal()
                } finally {
                    authorizationLock.unlock()
                }
            }
            TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                haveAuthorization = false
                print("Logging out")
            }
            TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                haveAuthorization = false
                print("Closing")
            }
            TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                print("Closed")
                if (!needQuit) {
                    ClientHolder.recreateClient()
                } else {
                    canQuit = true
                }
            }
            else -> System.err.println("Unsupported authorization state:" + newLine + authorizationState)
        }
    }
}
