package ru.smirnov.muteworkingchats.holder

import ru.smirnov.muteworkingchats.TdApi
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
    fun getChat(id: Long) = chats[id]

    fun sync(chatId: Long, block: () -> Unit) {
        val lock = locks.getOrPut(chatId) { ReentrantLock() }
        lock.withLock {
            block()
        }
    }
}
