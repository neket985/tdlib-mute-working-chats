package ru.smirnov.muteworkingchats

import ru.smirnov.muteworkingchats.Client.LogMessageHandler
import ru.smirnov.muteworkingchats.util.defaultHandler
import java.io.IOError
import java.io.IOException
import java.util.*

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
        "Enter command (me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): "

    private fun command() {
        val command = PromptService.promptString(commandsLine)
        val commands = command.split(" ".toRegex(), limit = 2).toTypedArray()
        try {
            when (commands[0]) {
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
