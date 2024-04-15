package ru.smirnov.muteworkingchats

import ru.smirnov.muteworkingchats.Client.LogMessageHandler
import ru.smirnov.muteworkingchats.holder.AuthorizationStateHolder
import ru.smirnov.muteworkingchats.holder.ClientHolder
import ru.smirnov.muteworkingchats.util.defaultHandler
import ru.smirnov.muteworkingchats.worker.*
import java.io.IOError
import java.io.IOException

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
        "Enter command (gfs - GetFolders, gfcs <folderId> - GetFolderChats, mute <folderId> - MuteFolderChats, unmute <folderId> - UnmuteFolderChats, me - GetMe, lo - LogOut, q - Quit): "

    private fun command() {
        val command = PromptService.promptString(commandsLine)
        val commands = command.split(" ".toRegex(), limit = 3).toTypedArray()
        try {
            when (commands[0]) {
                "gfs" -> GetFoldersWorker.work()
                "gfcs" -> GetFolderChatsWorker.work(commands[1].toInt())
                "gcs" -> GetChatsWorker.work()
                "mute" -> MuteFolderChatsWorker.work(commands[1].toInt())
                "unmute" -> UnmuteFolderChatsWorker.work(commands[1].toInt())
                "msgs" -> GetChatLastMessagesWorker.work(commands[1].toLong())
                "msg" -> GetMessageWorker.work(commands[1].toLong(), commands[2].toLong())
                "me" -> ClientHolder.getClient().send(TdApi.GetMe(), defaultHandler)
                "lo" -> AuthorizationStateHolder.logout()
                "q" -> AuthorizationStateHolder.quit()
                else -> System.err.println("Unsupported command: $command")
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            print("Not enough arguments")
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
