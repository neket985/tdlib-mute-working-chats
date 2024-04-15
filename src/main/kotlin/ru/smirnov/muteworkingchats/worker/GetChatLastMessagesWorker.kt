package ru.smirnov.muteworkingchats.worker

import ru.smirnov.muteworkingchats.holder.ChatsHolder

object GetChatLastMessagesWorker : WorkerI {
    fun work(chatId: Long) {
        ChatsHolder.getChat(chatId)?.also { chat ->
            println("last inbox [${chat.lastReadInboxMessageId}]")
            println("last outbox [${chat.lastReadOutboxMessageId}]")
        }
    }
}
