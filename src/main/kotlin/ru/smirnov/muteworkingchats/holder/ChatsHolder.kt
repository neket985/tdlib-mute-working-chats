package ru.smirnov.muteworkingchats.holder

import ru.smirnov.muteworkingchats.TdApi
import ru.smirnov.muteworkingchats.TdApi.Chat
import ru.smirnov.muteworkingchats.TdApi.GetChat
import ru.smirnov.muteworkingchats.util.newLine
import ru.smirnov.muteworkingchats.worker.MuteFolderChatsWorker.awaitForComplete
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object ChatsHolder {
    private val chats = ConcurrentHashMap<Long, TdApi.Chat>()
    private val locks = ConcurrentHashMap<Long, ReentrantLock>()

    fun updateState(update: TdApi.UpdateNewChat) = sync(update.chat.id) {
        chats[update.chat.id] = update.chat
    }

    fun updateState(update: TdApi.UpdateChatNotificationSettings) = sync(update.chatId) {
        chats[update.chatId]?.apply {
            notificationSettings = update.notificationSettings
        }
    }

    fun getChats() = chats.values
    fun getChat(id: Long) = chats[id] ?: run {
        awaitForComplete { future ->
            ClientHolder.getClient().send(GetChat(id)) {
                if (it is Chat) {
                    chats[id] = it
                } else {
                    System.err.println("Receive wrong response:$newLine$it")
                }
                future.complete(Unit)
            }
        }
        chats[id]
    }

    fun <T> sync(chatId: Long, block: () -> T): T {
        val lock = locks.getOrPut(chatId) { ReentrantLock() }
        return lock.withLock {
            block()
        }
    }
}
