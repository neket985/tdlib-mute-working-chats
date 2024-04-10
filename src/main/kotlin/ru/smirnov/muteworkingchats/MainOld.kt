//package ru.smirnov.muteworkingchats
//
//import java.io.BufferedReader
//import java.io.IOError
//import java.io.IOException
//import java.io.InputStreamReader
//import java.lang.Error
//import java.util.*
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.ConcurrentMap
//import java.util.concurrent.atomic.AtomicLong
//import java.util.concurrent.locks.Lock
//import java.util.concurrent.locks.ReentrantLock
//
///**
// * Example class for TDLib usage from Java.
// */
//object MainOld {
//    private var client: Client = Client.create(UpdateHandler(), null, null)
//
//    @Volatile
//    private var haveAuthorization = false
//
//    @Volatile
//    private var needQuit = false
//
//    @Volatile
//    private var canQuit = false
//    private val defaultHandler: Client.ResultHandler = DefaultHandler()
//    private val authorizationLock: Lock = ReentrantLock()
//    private val gotAuthorization = authorizationLock.newCondition()
//    private val users: ConcurrentMap<Long, TdApi.User> = ConcurrentHashMap<Long, TdApi.User>()
//    private val basicGroups: ConcurrentMap<Long, TdApi.BasicGroup> = ConcurrentHashMap<Long, TdApi.BasicGroup>()
//    private val supergroups: ConcurrentMap<Long, TdApi.Supergroup> = ConcurrentHashMap<Long, TdApi.Supergroup>()
//    private val secretChats: ConcurrentMap<Int, TdApi.SecretChat> = ConcurrentHashMap<Int, TdApi.SecretChat>()
//    private val chats: ConcurrentMap<Long, TdApi.Chat> = ConcurrentHashMap<Long, TdApi.Chat>()
//    private val mainChatList: NavigableSet<OrderedChat> = TreeSet()
//    private var haveFullMainChatList = false
//    private val usersFullInfo: ConcurrentMap<Long, TdApi.UserFullInfo> = ConcurrentHashMap<Long, TdApi.UserFullInfo>()
//    private val basicGroupsFullInfo: ConcurrentMap<Long, TdApi.BasicGroupFullInfo> =
//        ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>()
//    private val supergroupsFullInfo: ConcurrentMap<Long, TdApi.SupergroupFullInfo> =
//        ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>()
//    private val newLine = System.getProperty("line.separator")
//    private const val commandsLine =
//        "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): "
//
//
//    private fun setChatPositions(chat, positions: Array<TdApi.ChatPosition>) {
//        synchronized(mainChatList) {
//            synchronized(chat) {
//                for (position in chat.positions) {
//                    if (position.list.getConstructor() === TdApi.ChatListMain.CONSTRUCTOR) {
//                        val isRemoved =
//                            mainChatList.remove(OrderedChat(chat.id, position))
//                        assert(isRemoved)
//                    }
//                }
//                chat.positions = positions
//                for (position in chat.positions) {
//                    if (position.list.getConstructor() === TdApi.ChatListMain.CONSTRUCTOR) {
//                        val isAdded =
//                            mainChatList.add(OrderedChat(chat.id, position))
//                        assert(isAdded)
//                    }
//                }
//            }
//        }
//    }
//
//
//    private fun toInt(arg: String): Int {
//        var result = 0
//        try {
//            result = arg.toInt()
//        } catch (ignored: NumberFormatException) {
//        }
//        return result
//    }
//
//    private fun getChatId(arg: String): Long {
//        var chatId: Long = 0
//        try {
//            chatId = arg.toLong()
//        } catch (ignored: NumberFormatException) {
//        }
//        return chatId
//    }
//
//    private fun promptString(prompt: String): String {
//        print(prompt)
//        currentPrompt = prompt
//        val reader = BufferedReader(InputStreamReader(System.`in`))
//        var str = ""
//        try {
//            str = reader.readLine()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        currentPrompt = null
//        return str
//    }
//
//
//    private fun getMainChatList(limit: Int) {
//        synchronized(mainChatList) {
//            if (!haveFullMainChatList && limit > mainChatList.size) {
//                // send LoadChats request if there are some unknown chats and have not enough known chats
//                client.send(
//                    LoadChats(
//                        ChatListMain(),
//                        limit - mainChatList.size
//                    ), object : ResultHandler() {
//                        fun onResult(`object`: TdApi.Object) {
//                            when (`object`.getConstructor()) {
//                                TdApi.Error.CONSTRUCTOR -> if ((`object` as TdApi.Error).code === 404) {
//                                    synchronized(mainChatList) {
//                                        haveFullMainChatList = true
//                                    }
//                                } else {
//                                    System.err.println("Receive an error for LoadChats:" + newLine + `object`)
//                                }
//                                TdApi.Ok.CONSTRUCTOR ->                                 // chats had already been received through updates, let's retry request
//                                    getMainChatList(limit)
//                                else -> System.err.println("Receive wrong response from TDLib:" + newLine + `object`)
//                            }
//                        }
//                    })
//                return
//            }
//            val iter: Iterator<OrderedChat> =
//                mainChatList.iterator()
//            println()
//            println("First " + limit + " chat(s) out of " + mainChatList.size + " known chat(s):")
//            var i = 0
//            while (i < limit && i < mainChatList.size) {
//                val chatId = iter.next().chatId
//                val chat? = chats[chatId]
//                synchronized(chat) { println(chatId.toString() + ": " + chat.title) }
//                i++
//            }
//            print("")
//        }
//    }
//
//    private fun sendMessage(chatId: Long, message: String) {
//        // initialize reply markup just for testing
//        val row: Array<TdApi.InlineKeyboardButton> = arrayOf<TdApi.InlineKeyboardButton>(
//            InlineKeyboardButton(
//                "https://telegram.org?1",
//                InlineKeyboardButtonTypeUrl()
//            ),
//            InlineKeyboardButton("https://telegram.org?2", InlineKeyboardButtonTypeUrl()),
//            InlineKeyboardButton("https://telegram.org?3", InlineKeyboardButtonTypeUrl())
//        )
//        val replyMarkup: TdApi.ReplyMarkup =
//            ReplyMarkupInlineKeyboard(arrayOf<Array<TdApi.InlineKeyboardButton>>(row, row, row))
//        val content: TdApi.InputMessageContent = InputMessageText(FormattedText(message, null), null, true)
//        client.send(SendMessage(chatId, 0, null, null, replyMarkup, content), defaultHandler)
//    }
//
//    @Throws(InterruptedException::class)
//    @JvmStatic
//    fun main(args: Array<String>) {
//        // set log message handler to handle only fatal errors (0) and plain log messages (-1)
//        Client.setLogMessageHandler(0, LogMessageHandler())
//
//        // disable TDLib log and redirect fatal errors and plain log messages to a file
//        try {
//            Client.execute(SetLogVerbosityLevel(0))
//            Client.execute(SetLogStream(LogStreamFile("tdlib.log", 1 shl 27, false)))
//        } catch (error: Client.ExecutionException) {
//            throw IOError(IOException("Write access to the current directory is required"))
//        }
//
//        // main loop
//        while (!needQuit) {
//            // await authorization
//            authorizationLock.lock()
//            try {
//                while (!haveAuthorization) {
//                    gotAuthorization.await()
//                }
//            } finally {
//                authorizationLock.unlock()
//            }
//            while (haveAuthorization) {
//                command
//            }
//        }
//        while (!canQuit) {
//            Thread.sleep(1)
//        }
//    }
//
//    private fun onFatalError(errorMessage: String) {
//        class ThrowError private constructor(private val errorMessage: String, private val errorThrowTime: AtomicLong) :
//            Runnable {
//            override fun run() {
//                if (isDatabaseBrokenError(errorMessage) || isDiskFullError(errorMessage) || isDiskError(errorMessage)) {
//                    processExternalError()
//                    return
//                }
//                errorThrowTime.set(System.currentTimeMillis())
//                throw ClientError("TDLib fatal error: $errorMessage")
//            }
//
//            private fun processExternalError() {
//                errorThrowTime.set(System.currentTimeMillis())
//                throw ExternalClientError("Fatal error: $errorMessage")
//            }
//
//            inner class ClientError private constructor(message: String) : Error(message)
//            inner class ExternalClientError(message: String?) : Error(message)
//
//            private fun isDatabaseBrokenError(message: String): Boolean {
//                return message.contains("Wrong key or database is corrupted") ||
//                        message.contains("SQL logic error or missing database") ||
//                        message.contains("database disk image is malformed") ||
//                        message.contains("file is encrypted or is not a database") ||
//                        message.contains("unsupported file format") ||
//                        message.contains("Database was corrupted and deleted during execution and can't be recreated")
//            }
//
//            private fun isDiskFullError(message: String): Boolean {
//                return message.contains("PosixError : No space left on device") ||
//                        message.contains("database or disk is full")
//            }
//
//            private fun isDiskError(message: String): Boolean {
//                return message.contains("I/O error") || message.contains("Structure needs cleaning")
//            }
//        }
//
//        val errorThrowTime = AtomicLong(Long.MAX_VALUE)
//        Thread(ThrowError(errorMessage, errorThrowTime), "TDLib fatal error thread").start()
//
//        // wait at least 10 seconds after the error is thrown
//        while (errorThrowTime.get() >= System.currentTimeMillis() - 10000) {
//            try {
//                Thread.sleep(1000 /* milliseconds */)
//            } catch (ignore: InterruptedException) {
//                Thread.currentThread().interrupt()
//            }
//        }
//    }
//
//    private class DefaultHandler : Client.ResultHandler {
//        fun onResult(`object`: TdApi.Object) {
//            print(`object`.toString())
//        }
//    }
//
//    private class LogMessageHandler : LogMessageHandler {
//        fun onLogMessage(verbosityLevel: Int, message: String) {
//            if (verbosityLevel == 0) {
//                onFatalError(message)
//                return
//            }
//            System.err.println(message)
//        }
//    }
//}