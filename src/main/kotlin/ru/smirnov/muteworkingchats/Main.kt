package ru.smirnov.muteworkingchats

import ru.smirnov.muteworkingchats.Client.LogMessageHandler
import ru.smirnov.muteworkingchats.models.OrderedChat
import ru.smirnov.muteworkingchats.util.defaultHandler
import ru.smirnov.muteworkingchats.util.newLine
import java.io.IOError
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Example class for TDLib usage from Java.
 */
object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        // set log message handler to handle only fatal errors (0) and plain log messages (-1)
        Client.setLogMessageHandler(0, LogMessageHandlerImpl())

        // disable TDLib log and redirect fatal errors and plain log messages to a file
        try {
            Client.execute(TdApi.SetLogVerbosityLevel(0))
            Client.execute(TdApi.SetLogStream(TdApi.LogStreamFile("tdlib.log", 1 shl 27, false)))
        } catch (error: Client.ExecutionException) {
            throw IOError(IOException("Write access to the current directory is required"))
        }
        ClientHolder.getClient()

        // main loop
        while (true) {
            // await authorization
            AuthorizationStateHolder.waitGotAuthorisation()
            command()
        }
    }

    private const val commandsLine =
        "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): "

    private fun command() {
        val command = PromptService.promptString(commandsLine)
        val commands = command.split(" ".toRegex(), limit = 2).toTypedArray()
        try {
            when (commands[0]) {
                "gcs" -> {
                    var limit = 20
                    if (commands.size > 1) {
                        limit = commands[1].toInt()
                    }
                    getMainChatList(limit)
                }
                "me" -> ClientHolder.getClient().send(TdApi.GetMe(), defaultHandler)
                "lo" -> AuthorizationStateHolder.logout()
                "q" -> AuthorizationStateHolder.quit()
                else -> System.err.println("Unsupported command: $command")
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            print("Not enough arguments")
        }
    }

    private val chats: ConcurrentMap<Long, TdApi.Chat> = ConcurrentHashMap<Long, TdApi.Chat>()
    private val mainChatList: NavigableSet<OrderedChat> = TreeSet()
    private var haveFullMainChatList = false
    private fun getMainChatList(limit: Int) {
        synchronized(mainChatList) {
            if (!haveFullMainChatList && limit > mainChatList.size) {
                // send LoadChats request if there are some unknown chats and have not enough known chats
                ClientHolder.getClient().send(
                    TdApi.LoadChats(
                        TdApi.ChatListMain(),
                        limit - mainChatList.size,
                    ),
                ) { obj ->
                    when (obj.constructor) {
                        TdApi.Error.CONSTRUCTOR -> if ((obj as TdApi.Error).code === 404) {
                            synchronized(mainChatList) {
                                haveFullMainChatList = true
                            }
                        } else {
                            System.err.println("Receive an error for LoadChats:" + newLine + obj)
                        }
                        TdApi.Ok.CONSTRUCTOR -> // chats had already been received through updates, let's retry request
                            getMainChatList(limit)
                        else -> System.err.println("Receive wrong response from TDLib:" + newLine + obj)
                    }
                }
                return
            }
            val iter = mainChatList.iterator()
            println()
            println("First " + limit + " chat(s) out of " + mainChatList.size + " known chat(s):")
            var i = 0
            while (i < limit && i < mainChatList.size) {
                val chatId = iter.next().chatId
                val chat = chats[chatId]
                println(chatId.toString() + ": " + chat?.title)
                i++
            }
            print("")
        }
    }

    private class LogMessageHandlerImpl : LogMessageHandler {
        override fun onLogMessage(verbosityLevel: Int, message: String) {
            if (verbosityLevel == 0) {
                throw RuntimeException("Received fatal error [$message]")
            }
            System.err.println(message)
        }
    }
}
